package com.anfema.ionclient

import android.content.Context
import com.anfema.ionclient.archive.IonArchive
import com.anfema.ionclient.archive.IonArchiveDownloader
import com.anfema.ionclient.mediafiles.FileWithStatus
import com.anfema.ionclient.mediafiles.IonFiles
import com.anfema.ionclient.mediafiles.IonFilesWithCaching
import com.anfema.ionclient.pages.IonPages
import com.anfema.ionclient.pages.IonPagesFactory
import com.anfema.ionclient.pages.models.Collection
import com.anfema.ionclient.pages.models.Page
import com.anfema.ionclient.pages.models.PagePreview
import com.anfema.ionclient.pages.models.contents.Downloadable
import com.anfema.ionclient.utils.IonLog
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Predicate
import okhttp3.HttpUrl
import java.io.File

/**
 * Main entry point for ION functionality. Obtain an instance with [.getInstance].
 *
 *
 * Serving as entry point IonClient holds interfaces providing the actual implementation of its functionality.
 */
class IonClient private constructor(config: IonConfig, context: Context) : IonPages, IonFiles, IonArchive {

    companion object {
        /// Multiton
        private val instances: MutableMap<IonConfig, IonClient> = HashMap()

        /**
         * @param config configuration for ION client
         * @return client instance, ready to go
         */
        @JvmStatic
        fun getInstance(config: IonConfig, context: Context): IonClient {
            IonConfig.assertConfigIsValid(config)

            // check if client for this configuration already exists, otherwise create an instance
            val storedClient = instances[config]
            if (storedClient?.context != null) {
                // update config because values, which are not included in equality check, might have changed
                storedClient.updateConfig(config)
                return storedClient
            }
            val ionClient = IonClient(config, context.applicationContext)
            instances[config] = ionClient
            IonLog.d("IonClient", "# ION client instances: " + instances.size)
            return ionClient
        }
        /// Multiton END
    }

    // stored to verify on #getInstance(IonConfig, Context) that context (which is passed to delegate classes) is not null.
    private val context: Context = context.applicationContext

    // delegate classes
    private val ionPages: IonPages
    private val ionFiles: IonFiles
    private val ionArchive: IonArchive

    init {
        ionPages = IonPagesFactory.newInstance(config, context)
        ionFiles = IonFilesWithCaching(config, context)
        ionArchive = IonArchiveDownloader(ionPages, ionFiles, config, context)
    }

    override fun updateConfig(config: IonConfig) {
        ionPages.updateConfig(config)
        ionFiles.updateConfig(config)
        ionArchive.updateConfig(config)
    }

    /// Collection and page calls
    /**
     * Call collections on Ion API.
     * Adds collection identifier and authorization token to request as retrieved via [IonConfig]<br></br>
     */
    override fun fetchCollection(): Single<Collection> =
        ionPages.fetchCollection()

    override fun fetchCollection(preferNetwork: Boolean): Single<Collection> =
        ionPages.fetchCollection(preferNetwork)

    override fun fetchPagePreview(pageIdentifier: String): Single<PagePreview> =
        ionPages.fetchPagePreview(pageIdentifier)

    /**
     * A set of page previews is "returned" by emitting multiple events.
     *
     * @param pagesFilter see [com.anfema.ionclient.utils.PagesFilter] for some frequently used filter options.
     */
    override fun fetchPagePreviews(pagesFilter: Predicate<PagePreview>): Observable<PagePreview> =
        ionPages.fetchPagePreviews(pagesFilter)

    override fun fetchAllPagePreviews(): Observable<PagePreview> =
        ionPages.fetchAllPagePreviews()

    /**
     * Add collection identifier and authorization token to request.<br></br>
     */
    override fun fetchPage(pageIdentifier: String): Single<Page> =
        ionPages.fetchPage(pageIdentifier)

    override fun fetchPages(pageIdentifiers: List<String>): Observable<Page> =
        ionPages.fetchPages(pageIdentifiers)

    /**
     * A set of pages is "returned" by emitting multiple events.<br></br>
     *
     * @param pagesFilter see [com.anfema.ionclient.utils.PagesFilter] for some frequently used filter options.
     */
    override fun fetchPages(pagesFilter: Predicate<PagePreview>): Observable<Page> =
        ionPages.fetchPages(pagesFilter)

    /**
     * A set of pages is "returned" by emitting multiple events.<br></br>
     */
    override fun fetchAllPages(): Observable<Page> =
        ionPages.fetchAllPages()

    // Loading media files
    override fun request(content: Downloadable): Single<FileWithStatus> =
        ionFiles.request(content)

    override fun request(url: HttpUrl, checksum: String): Single<FileWithStatus> =
        ionFiles.request(url, checksum)

    override fun request(
        url: HttpUrl,
        checksum: String,
        ignoreCaching: Boolean,
        targetFile: File?,
    ): Single<FileWithStatus> =
        ionFiles.request(url, checksum, ignoreCaching, targetFile)

    override fun request(
        url: HttpUrl,
        downloadUrl: HttpUrl,
        checksum: String,
        ignoreCaching: Boolean,
        targetFile: File?,
    ): Single<FileWithStatus> =
        ionFiles.request(url, downloadUrl, checksum, ignoreCaching, targetFile)

    /// Archive download
    /**
     * @see IonArchive.downloadArchive
     */
    override fun downloadArchive(): Completable =
        ionArchive.downloadArchive()
}
