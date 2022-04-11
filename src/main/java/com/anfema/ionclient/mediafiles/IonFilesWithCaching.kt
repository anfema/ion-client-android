package com.anfema.ionclient.mediafiles

import android.content.Context
import com.anfema.ionclient.CachingStrategy
import com.anfema.ionclient.CollectionProperties
import com.anfema.ionclient.caching.FilePaths
import com.anfema.ionclient.caching.index.CollectionCacheIndex
import com.anfema.ionclient.caching.index.FileCacheIndex
import com.anfema.ionclient.exceptions.FileNotAvailableException
import com.anfema.ionclient.exceptions.HttpException
import com.anfema.ionclient.okhttp.filesOkHttpClient
import com.anfema.ionclient.pages.models.contents.Downloadable
import com.anfema.ionclient.utils.DateTimeUtils
import com.anfema.ionclient.utils.FileUtils
import com.anfema.ionclient.utils.IonLog
import com.anfema.ionclient.utils.PendingDownloadHandler
import com.anfema.utils.NetworkUtils
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException

/**
 * Does not perform calls against a specific API, but takes complete URLs as parameter to perform a GET call to.
 *
 *
 * Downloads the response body and stores it into a file.
 *
 *
 * However, the ION authorization header is added (in case the URL points to protected media).
 */
internal class IonFilesWithCaching(
    sharedOkHttpClient: OkHttpClient,
    override val collectionProperties: CollectionProperties,
    private val context: Context,
    private val cachingStrategy: CachingStrategy,
) : IonFiles {

    private val filesOkHttpClient = filesOkHttpClient(sharedOkHttpClient, collectionProperties)

    private val runningDownloads: PendingDownloadHandler<HttpUrl, File> = PendingDownloadHandler()

    override fun request(content: Downloadable): Single<FileWithStatus> =
        request(content.url.toHttpUrl(), content.checksum)

    override fun request(
        url: HttpUrl,
        downloadUrl: HttpUrl,
        checksum: String?,
        ignoreCaching: Boolean,
        targetFile: File?,
    ): Single<FileWithStatus> {

        val networkAvailable =
            NetworkUtils.isConnected(context) && cachingStrategy != CachingStrategy.STRICT_OFFLINE

        val targetFile = getTargetFilePath(url, targetFile)

        if (ignoreCaching) {
            return if (networkAvailable) {
                // force new download, do not create cache index entry
                requestAndSaveToFile(downloadUrl, targetFile)
                    .map { file: File? -> FileWithStatus(file, FileStatus.NETWORK) }
                    .subscribeOn(Schedulers.io())
            } else {
                Single.error(FileNotAvailableException(url))
            }
        }

        // fetch file from local storage or download it?
        return when {
            targetFile.exists() && isFileUpToDate(url, checksum) -> {
                // retrieve current version from cache
                IonLog.i("File Cache Lookup", url.toString())
                Single.just(targetFile)
                    .map { file: File? -> FileWithStatus(file, FileStatus.DISK) }
            }
            networkAvailable -> {
                val requestTime = DateTimeUtils.now()
                // download media file
                val downloadSingle = requestAndSaveToFile(downloadUrl, targetFile)
                    .doOnSuccess { file: File ->
                        FileCacheIndex.save(url.toString(), file, collectionProperties, null, requestTime, context)
                    }
                    .subscribeOn(Schedulers.io())
                    .doFinally { runningDownloads.finished(url) }

                runningDownloads.starting(url, downloadSingle.toObservable()).singleOrError()
                    .map { file: File? -> FileWithStatus(file, FileStatus.NETWORK) }
            }
            targetFile.exists() -> {
                // no network: use old version from cache (even if no cache index entry exists)
                IonLog.i("File Cache Lookup", url.toString())
                Single.just(targetFile)
                    .map { file: File? -> FileWithStatus(file, FileStatus.DISK_OUTDATED) }
            }
            else -> {
                // media file can neither be downloaded nor be found in cache
                Single.error(FileNotAvailableException(url))
            }
        }
    }

    private fun isFileUpToDate(url: HttpUrl, checksum: String?): Boolean {
        val fileCacheIndex = FileCacheIndex.retrieve(url.toString(), collectionProperties, context) ?: return false
        return if (checksum != null) {
            // check with file's checksum
            !fileCacheIndex.isOutdated(checksum)
        } else {
            // check with collection's last_modified (previewPage.last_changed would be slightly more precise)
            val collectionCacheIndex = CollectionCacheIndex.retrieve(collectionProperties, context)
            val collectionLastModified = collectionCacheIndex?.lastModifiedDate
            val fileLastUpdated = fileCacheIndex.lastUpdated
            collectionLastModified != null && !collectionLastModified.isAfter(fileLastUpdated)
        }
    }

    /**
     * Request and store response body to local storage.
     *
     * @param url        source location of content
     * @param targetFile path, where file is going to be stored. if null, default "/files" directory is used
     * @return the file with content
     */
    private fun requestAndSaveToFile(url: HttpUrl, targetFile: File?): Single<File> {
        return performRequest(url)
            .flatMap { response: Response -> writeToLocalStorage(response, targetFile) }
    }

    /**
     * Perform get request
     */
    private fun performRequest(url: HttpUrl): Single<Response> {

        val request: Request = Request.Builder().url(url).build()

        return try {
            val response = filesOkHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                val responseBody = response.body
                responseBody?.close()
                return Single.error(HttpException(response.code, response.message))
            }

            // use custom target file path
            Single.just(response)
        } catch (e: IOException) {
            Single.error(e)
        }
    }

    /**
     * write from input stream to file
     */
    private fun writeToLocalStorage(response: Response, targetFile: File?): Single<File> {
        // Be aware: using this method empties the response body byte stream. It is not possible to read the response a second time.
        val inputStream = response.body!!.byteStream()

        val file: File? = try {
            inputStream.use { FileUtils.writeToFile(it, targetFile) }
        } catch (e: IOException) {
            return Single.error(e)
        }
        return if (file == null) {
            Single.error(IOException("Failure writing " + targetFile!!.path + " to local storage."))
        } else Single.just(file)
    }

    /**
     * Request method does not require that a file path is provided via {@param targetFile}.
     * If a custom path is provided, it is used. If `null` is passed, then the default file path for media files is calculated from the {@param url}.
     *
     * @param url        HTTP URL for an ION internal media file
     * @param targetFile custom file path (optional)
     * @return {@param targetFile} or default file path
     */
    private fun getTargetFilePath(url: HttpUrl, targetFile: File?): File =
        targetFile ?: FilePaths.getMediaFilePath(url.toString(), collectionProperties, context)
}
