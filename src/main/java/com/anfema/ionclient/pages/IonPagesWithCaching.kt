package com.anfema.ionclient.pages

import android.content.Context
import com.anfema.ionclient.CachingStrategy
import com.anfema.ionclient.CollectionProperties
import com.anfema.ionclient.caching.FilePaths
import com.anfema.ionclient.caching.MemoryCache
import com.anfema.ionclient.caching.index.CollectionCacheIndex
import com.anfema.ionclient.caching.index.PageCacheIndex
import com.anfema.ionclient.exceptions.CollectionNotAvailableException
import com.anfema.ionclient.exceptions.NetworkRequestException
import com.anfema.ionclient.exceptions.PageNotAvailableException
import com.anfema.ionclient.exceptions.ReadFromCacheException
import com.anfema.ionclient.okhttp.pagesOkHttpClient
import com.anfema.ionclient.pages.IonPageUrls.getCollectionUrl
import com.anfema.ionclient.pages.IonPageUrls.getPageUrl
import com.anfema.ionclient.pages.models.Collection
import com.anfema.ionclient.pages.models.Page
import com.anfema.ionclient.pages.models.PagePreview
import com.anfema.ionclient.pages.models.responses.CollectionResponse
import com.anfema.ionclient.pages.models.responses.PageResponse
import com.anfema.ionclient.serialization.GsonHolder.defaultInstance
import com.anfema.ionclient.utils.DateTimeUtils
import com.anfema.ionclient.utils.FileUtils
import com.anfema.ionclient.utils.IonLog
import com.anfema.ionclient.utils.PagesFilter
import com.anfema.ionclient.utils.PendingDownloadHandler
import com.anfema.utils.NetworkUtils
import com.anfema.utils.byteCount
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Predicate
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import retrofit2.HttpException
import retrofit2.Response

/**
 * A wrapper of "collections" and "pages" call of ION API.
 *
 *
 * Adds collection identifier and authorization token to request as retrieved via [CollectionProperties]<br></br>
 *
 *
 * Uses a file and a memory cache.
 */
internal class IonPagesWithCaching(
    sharedOkHttpClient: OkHttpClient,
    private val collectionProperties: CollectionProperties,
    private val collectionRefetchInMin: Int,
    private val cachingStrategy: CachingStrategy,
    private val context: Context,
) : IonPages {

    companion object {
        const val COLLECTION_NOT_MODIFIED = 304

        /**
         * Find out size of response body and calculate how much space it takes in a Java string,
         * or - if header is not set - (conservatively) assume it is 100 KB.
         *
         * @return unit: bytes
         */
        private fun getContentByteCount(response: Response<*>): Int {
            val contentLength = response.headers()["Content-Length"]
            return if (contentLength != null) contentLength.toInt() * 2 else 100 * 1024
        }
    }

    private val _onCollectionDownloaded = PublishSubject.create<CollectionDownloaded>()
    override val onCollectionDownloaded: Observable<CollectionDownloaded> = _onCollectionDownloaded

    //key: collection identifier
    private val runningCollectionDownload: PendingDownloadHandler<String, Collection>

    //key: page identifier
    private val runningPageDownloads: PendingDownloadHandler<String, Page>

    /**
     * Access to the ION API
     */
    private val ionApi: RetrofitIonPagesApi

    init {
        val pagesOkHttpClient = pagesOkHttpClient(sharedOkHttpClient, collectionProperties, context)
        ionApi = retrofitIonPagesApi(pagesOkHttpClient, collectionProperties.baseUrl)
        runningCollectionDownload = PendingDownloadHandler()
        runningPageDownloads = PendingDownloadHandler()
    }

    /**
     * Retrieve collection. Strategy depends on [cachingStrategy].
     * Use default collection identifier as specified in [collectionProperties]
     */
    override fun fetchCollection(): Single<Collection> =
        fetchCollection(false)

    /**
     * @param preferNetwork try network download as first option if set to false
     */
    override fun fetchCollection(preferNetwork: Boolean): Single<Collection> {
        val cacheIndex = CollectionCacheIndex.retrieve(collectionProperties, context)
        val currentCacheEntry = cacheIndex != null && !cacheIndex.isOutdated(collectionRefetchInMin)
        val networkAvailable = NetworkUtils.isConnected(context) && cachingStrategy !== CachingStrategy.STRICT_OFFLINE

        return if (currentCacheEntry && !preferNetwork) {
            // retrieve current version from cache
            fetchCollectionFromCache(cacheIndex, networkAvailable)
        } else if (networkAvailable) {
            // download collection
            fetchCollectionFromServer(cacheIndex)
        } else if (cacheIndex != null) {
            // no network: use old version from cache
            // TODO notify app that data might be outdated
            fetchCollectionFromCache(cacheIndex, false)
        } else {
            // collection can neither be downloaded nor be found in cache
            Single.error(CollectionNotAvailableException())
        }
    }

    override fun fetchPagePreview(pageIdentifier: String): Single<PagePreview> =
        fetchCollection()
            .map { collection: Collection -> collection.pages }
            .flatMapObservable { source: ArrayList<PagePreview>? -> Observable.fromIterable(source) }
            .filter(PagesFilter.identifierEquals(pageIdentifier))
            .singleOrError()

    override fun fetchPagePreviews(pagesFilter: Predicate<PagePreview>): Observable<PagePreview> =
        fetchCollection()
            .map { collection: Collection -> collection.pages }
            .flatMapObservable { source: ArrayList<PagePreview>? -> Observable.fromIterable(source) }
            .filter(pagesFilter)

    override fun fetchAllPagePreviews(): Observable<PagePreview> =
        fetchPagePreviews(PagesFilter.ALL)

    /**
     * Retrieve page with following priorities:
     *
     *
     * 1. Look if there is a current version in cache
     * 2. Download from server if internet connection available
     * 3. If no internet connection: return cached version (even if outdated)
     * 4. Collection is not retrievable at all: emit error
     *
     *
     * Add collection identifier and authorization token to request.<br></br>
     * Use default collection identifier as specified in [collectionProperties]
     */
    override fun fetchPage(pageIdentifier: String): Single<Page> {
        val pageUrl = collectionProperties.getPageUrl(pageIdentifier)

        val pageCacheIndex = PageCacheIndex.retrieve(pageUrl, collectionProperties, context)

        return if (pageCacheIndex != null) {

            fetchCollection() // get page's last_changed date from collection
                .flatMap { collection: Collection -> collection.getPageLastChangedAsync(pageIdentifier) } // compare last_changed date of cached page with that of collection
                .map { serverDate: DateTime -> pageCacheIndex.isOutdated(serverDate) }
                .flatMap { isOutdated: Boolean ->
                    val networkAvailable =
                        NetworkUtils.isConnected(context) && cachingStrategy !== CachingStrategy.STRICT_OFFLINE

                    when {
                        !isOutdated -> {
                            // current version available
                            fetchPageFromCache(pageIdentifier, networkAvailable)
                        }
                        networkAvailable -> {
                            // download page
                            fetchPageFromServer(pageIdentifier)
                        }
                        else -> {
                            // no network available, but an old cached version exists
                            fetchPageFromCache(pageIdentifier, false)
                        }
                    }
                }
        } else {
            // no cached version available: There is no need to fetch collection for date comparison.
            if (NetworkUtils.isConnected(context)) {
                // download page
                fetchPageFromServer(pageIdentifier)
            } else {
                // page can neither be downloaded nor be found in cache
                Single.error(PageNotAvailableException())
            }
        }
    }

    /**
     * Fetch a set of pages by passing its page identifiers.
     * This is a convenience method for [.fetchPages].
     */
    override fun fetchPages(pageIdentifiers: List<String>): Observable<Page> =
        Observable.fromIterable(pageIdentifiers)
            .concatMapSingle { pageIdentifier: String -> fetchPage(pageIdentifier) }

    /**
     * A set of pages is "returned" by emitting multiple events.<br></br>
     * Use collection identifier as specified in [collectionProperties]
     */
    override fun fetchPages(pagesFilter: Predicate<PagePreview>): Observable<Page> =
        fetchCollection()
            .map { collection: Collection -> collection.pages }
            .flatMapObservable { source: ArrayList<PagePreview>? -> Observable.fromIterable(source) }
            .filter(pagesFilter)
            .map { page: PagePreview -> page.identifier }
            .concatMapSingle { pageIdentifier: String -> fetchPage(pageIdentifier) }

    /**
     * A set of pages is "returned" by emitting multiple events.<br></br>
     * Use collection identifier as specified in [collectionProperties]
     */
    override fun fetchAllPages(): Observable<Page> =
        fetchPages(PagesFilter.ALL)

    /// Get collection methods
    private fun fetchCollectionFromCache(
        cacheIndex: CollectionCacheIndex?,
        serverCallAsBackup: Boolean,
    ): Single<Collection> {
        val collectionUrl = collectionProperties.getCollectionUrl()

        // retrieve from memory cache
        val collection = MemoryCache.getCollection(collectionUrl)
        if (collection != null) {
            IonLog.i("Memory Cache Lookup", collectionUrl)
            return Single.just(collection)
        }

        // retrieve from file cache
        IonLog.i("File Cache Lookup", collectionUrl)
        val filePath = FilePaths.getCollectionJsonPath(collectionUrl, collectionProperties, context)
        return if (!filePath.exists()) {
            Single.error(CollectionNotAvailableException())
        } else FileUtils.readTextFromFile(filePath) // deserialize collection and remember byte count
            .map { collectionsString: String ->
                val collectionResponse = defaultInstance.fromJson(collectionsString, CollectionResponse::class.java)
                val collection1 = collectionResponse.collection
                collection1.byteCount = collectionsString.byteCount()
                collection1
            } // save to memory cache
            .doOnSuccess { collection1: Collection? -> MemoryCache.saveCollection(collection1, collectionUrl, context) }
            .onErrorResumeNext { throwable: Throwable ->
                handleUnsuccessfulCollectionCacheReading(collectionUrl,
                    cacheIndex,
                    serverCallAsBackup,
                    throwable)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun handleUnsuccessfulCollectionCacheReading(
        collectionUrl: String,
        cacheIndex: CollectionCacheIndex?,
        serverCallAsBackup: Boolean,
        e: Throwable,
    ): Single<Collection> {
        return if (serverCallAsBackup) {
            IonLog.w("Backup Request", "Cache lookup $collectionUrl failed. Trying network request instead...")
            IonLog.ex("Cache Lookup", e)
            fetchCollectionFromServer(cacheIndex)
        } else {
            IonLog.e("Failed Request", "Cache lookup $collectionUrl failed.")
            Single.error(ReadFromCacheException(collectionUrl,
                e))
        }
    }

    /**
     * Download collection from server.
     * Adds collection identifier and authorization token to request.<br></br>
     * Uses default collection identifier as specified in [collectionProperties]
     */
    private fun fetchCollectionFromServer(cacheIndex: CollectionCacheIndex?): Single<Collection> {
        val lastModified = cacheIndex?.lastModified
        val requestTime = DateTimeUtils.now()
        val collectionSingle =
            ionApi.getCollection(collectionProperties.collectionIdentifier,
                collectionProperties.locale,
                collectionProperties.variation,
                lastModified)
                .flatMap { serverResponse: Response<CollectionResponse> ->
                    when {
                        serverResponse.code() == COLLECTION_NOT_MODIFIED -> {
                            // collection has not changed, return cached version
                            fetchCollectionFromCache(cacheIndex,
                                false) // update cache index again (last updated needs to be reset to now)
                                .doOnSuccess { saveCollectionCacheIndex(lastModified, requestTime) }
                        }
                        serverResponse.isSuccessful -> {
                            val lastModifiedReceived = serverResponse.headers()["Last-Modified"]

                            // parse collection data from response and write cache index and memory cache
                            Single.just(serverResponse) // unwrap page and remember byte count
                                .map { response: Response<CollectionResponse> ->
                                    val collection = response.body()!!.collection
                                    collection.byteCount = getContentByteCount(response).toLong()
                                    collection
                                }
                                .doOnSuccess { collection: Collection ->
                                    MemoryCache.saveCollection(collection, collectionProperties, context)
                                }
                                .doOnSuccess { saveCollectionCacheIndex(lastModifiedReceived, requestTime) }
                                .doOnSuccess { collection: Collection ->
                                    _onCollectionDownloaded.onNext(CollectionDownloaded(collection,
                                        lastModifiedReceived))
                                }
                        }
                        else -> {
                            Single.error(HttpException(serverResponse))
                        }
                    }
                }
                .onErrorResumeNext { throwable: Throwable? ->
                    val collectionUrl = collectionProperties.getCollectionUrl()
                    IonLog.e("Failed Request", "Network request $collectionUrl failed.")
                    Single.error(NetworkRequestException(collectionUrl, throwable))
                }
                .subscribeOn(Schedulers.io())
                .doFinally { runningCollectionDownload.finished(collectionProperties.collectionIdentifier) }

        return runningCollectionDownload.starting(collectionProperties.collectionIdentifier,
            collectionSingle.toObservable())
            .singleOrError()
    }
    /// Get collection methods END

    /// Get page methods
    /**
     * @param serverCallAsBackup If reading from cache is not successful, should a server call be made?
     */
    private fun fetchPageFromCache(pageIdentifier: String, serverCallAsBackup: Boolean): Single<Page> {
        val pageUrl = collectionProperties.getPageUrl(pageIdentifier)

        // retrieve from memory cache
        val memPage = MemoryCache.getPage(pageUrl)
        if (memPage != null) {
            IonLog.i("Memory Cache Lookup", pageUrl)
            return Single.just(memPage)
        }

        // retrieve from file cache
        IonLog.i("File Cache Lookup", pageUrl)
        val filePath = FilePaths.getPageJsonPath(pageUrl, pageIdentifier, collectionProperties, context)
        return if (!filePath.exists()) {
            handleUnsuccessfulPageCacheReading(pageIdentifier, serverCallAsBackup, pageUrl, PageNotAvailableException())
        } else FileUtils.readTextFromFile(filePath) // deserialize page and remember byte count
            .map { pagesString: String ->
                val pageResponse = defaultInstance.fromJson(pagesString, PageResponse::class.java)
                val page = pageResponse.page
                page.byteCount = pagesString.byteCount()
                page
            } // save to memory cache
            .doOnSuccess { page: Page? -> MemoryCache.savePage(page, collectionProperties, context) }
            .onErrorResumeNext { throwable: Throwable ->
                handleUnsuccessfulPageCacheReading(pageIdentifier,
                    serverCallAsBackup,
                    pageUrl,
                    throwable)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun handleUnsuccessfulPageCacheReading(
        pageIdentifier: String,
        serverCallAsBackup: Boolean,
        pageUrl: String,
        e: Throwable,
    ): Single<Page> =
        if (serverCallAsBackup) {
            IonLog.w("Backup Request", "Cache lookup $pageUrl failed. Trying network request instead...")
            IonLog.ex("Cache Lookup", e)
            fetchPageFromServer(pageIdentifier)
        } else {
            IonLog.e("Failed Request", "Cache lookup $pageUrl failed.")
            Single.error(ReadFromCacheException(pageUrl, e))
        }

    private fun fetchPageFromServer(pageIdentifier: String): Single<Page> {
        val pageSingle =
            ionApi.getPage(collectionProperties.collectionIdentifier,
                pageIdentifier,
                collectionProperties.locale,
                collectionProperties.variation)
                .map { response: Response<PageResponse> ->
                    // unwrap page and remember byte count
                    if (response.isSuccessful) {
                        val page = response.body()!!.page.apply { }
                        page.byteCount = getContentByteCount(response).toLong()
                        page
                    } else {
                        throw HttpException(response)
                    }
                }
                .doOnSuccess { page: Page -> PageCacheIndex.save(page, collectionProperties, context) }
                .doOnSuccess { page: Page -> MemoryCache.savePage(page, collectionProperties, context) }
                .onErrorResumeNext { throwable: Throwable ->
                    val pageUrl = collectionProperties.getPageUrl(pageIdentifier)
                    IonLog.e("Failed Request", "Network request $pageUrl failed.")
                    Single.error(NetworkRequestException(pageUrl, throwable))
                }
                .subscribeOn(Schedulers.io())
                .doFinally { runningPageDownloads.finished(pageIdentifier) }

        return runningPageDownloads.starting(pageIdentifier, pageSingle.toObservable()).singleOrError()
    }

    /// Get page methods END

    private fun saveCollectionCacheIndex(lastModified: String?, lastUpdated: DateTime) {
        CollectionCacheIndex.save(collectionProperties, context, lastModified, lastUpdated)
    }
}
