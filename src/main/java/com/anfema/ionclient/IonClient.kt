package com.anfema.ionclient

import android.content.Context
import com.anfema.ionclient.archive.IonArchive
import com.anfema.ionclient.archive.IonArchiveDownloader
import com.anfema.ionclient.caching.CacheCompatManager
import com.anfema.ionclient.mediafiles.FileWithStatus
import com.anfema.ionclient.mediafiles.IonFiles
import com.anfema.ionclient.mediafiles.IonFilesWithCaching
import com.anfema.ionclient.pages.IonPagesWithCaching
import com.anfema.ionclient.pages.models.Page
import com.anfema.ionclient.pages.models.PagePreview
import com.anfema.ionclient.pages.models.contents.Downloadable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Predicate
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

/**
 * Defines strategies, when to fetch data from cache and when to download it from internet.
 */
enum class CachingStrategy {
    /**
     * strategy:
     * 1. fetch current version from cache
     * 2. download current version (if connected to internet)
     * 3. fetch possibly outdated version from cache (if it exists)
     * 4. error (because no version in cache exists and no internet connection)
     */
    NORMAL,

    /**
     * strategy:
     * 1. fetch (possibly outdated) version from cache (if it exists)
     * 2. error (because no version in cache exists and downloading is prohibited with this mode)
     */
    STRICT_OFFLINE
}

/**
 * Main entry point for ION functionality.
 * Serving as entry point IonClient holds interfaces providing the actual implementation of its functionality.
 *
 * [context] must be the application context.
 */
data class IonClient @JvmOverloads constructor(
    @JvmField
    val config: IonConfig,
    @JvmField
    val context: Context,
    @JvmField
    val sharedOkHttpClient: OkHttpClient,
    @JvmField
    val cachingStrategy: CachingStrategy = CachingStrategy.NORMAL,
) {
    // delegate classes
    private val ionPages = IonPagesWithCaching(sharedOkHttpClient, config, context, cachingStrategy)
    private val ionFiles: IonFiles = IonFilesWithCaching(sharedOkHttpClient, config, context, cachingStrategy)
    private val ionArchive: IonArchive = IonArchiveDownloader(ionPages, ionFiles, config, context)

    init {
        CacheCompatManager.clearCacheIfIncompatible(context)
    }

    fun fetchPagePreview(pageIdentifier: String): Single<PagePreview> =
        ionPages.fetchPagePreview(pageIdentifier)

    /**
     * A set of page previews is "returned" by emitting multiple events.
     *
     * @param pagesFilter see [com.anfema.ionclient.utils.PagesFilter] for some frequently used filter options.
     */
    fun fetchPagePreviews(pagesFilter: Predicate<PagePreview>): Observable<PagePreview> =
        ionPages.fetchPagePreviews(pagesFilter)

    /**
     * Add collection identifier and authorization token to request.<br></br>
     */
    fun fetchPage(pageIdentifier: String): Single<Page> =
        ionPages.fetchPage(pageIdentifier)

    fun fetchPages(pageIdentifiers: List<String>): Observable<Page> =
        ionPages.fetchPages(pageIdentifiers)

    /**
     * A set of pages is "returned" by emitting multiple events.<br></br>
     *
     * @param pagesFilter see [com.anfema.ionclient.utils.PagesFilter] for some frequently used filter options.
     */
    fun fetchPages(pagesFilter: Predicate<PagePreview>): Observable<Page> =
        ionPages.fetchPages(pagesFilter)

    /**
     * A set of pages is "returned" by emitting multiple events.<br></br>
     */
    fun fetchAllPages(): Observable<Page> =
        ionPages.fetchAllPages()

    /**
     * Retrieve a file through its URL either from file cache or with a network request. The result can be cached for further requests.
     */
    fun request(content: Downloadable): Single<FileWithStatus> =
        ionFiles.request(content)

    /**
     * Retrieve a file through its URL either from file cache or with a network request. The result can be cached for further requests.
     */
    fun request(url: HttpUrl, checksum: String?): Single<FileWithStatus> =
        ionFiles.request(url, checksum)

    /**
     * @see IonArchive.downloadArchive
     */
    fun downloadArchive(): Completable =
        ionArchive.downloadArchive()
}
