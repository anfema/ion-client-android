package com.anfema.ionclient.interceptors

import android.content.Context
import com.anfema.ionclient.CachingStrategy
import com.anfema.ionclient.CollectionProperties
import com.anfema.ionclient.caching.FilePaths
import com.anfema.ionclient.caching.index.CollectionCacheIndex
import com.anfema.ionclient.caching.index.FileCacheIndex
import com.anfema.ionclient.exceptions.FileNotAvailableException
import com.anfema.ionclient.utils.DateTimeUtils
import com.anfema.ionclient.utils.FileUtils
import com.anfema.ionclient.utils.IonLog
import com.anfema.utils.NetworkUtils
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection.HTTP_OK

/**
 * Full caching functionality for (media) files.
 * Does however not work for ION pages API.
 */
internal class IonFileCacheInterceptor(
    private val collectionProperties: CollectionProperties,
    private val context: Context,
    private val cachingStrategy: CachingStrategy = CachingStrategy.NORMAL,
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val url = request.url

        val networkAvailable =
            NetworkUtils.isConnected(context) && cachingStrategy != CachingStrategy.STRICT_OFFLINE
        val targetFile: File = FilePaths.getFilePath(url.toString(), collectionProperties, context)

        // fetch file from local storage or download it?

        // TODO add checksum via header?
        val checksum = null

        return when {
            targetFile.exists() && isFileUpToDate(url, checksum) -> {
                // retrieve current version from cache
                IonLog.i("File Cache Lookup", url.toString())

                responseFromCache(targetFile, request)
            }
            networkAvailable -> {
                val requestTime = DateTimeUtils.now()

                chain.proceed(request).also { response ->

                    if (response.isSuccessful) {
                        val cachedFile = response.writeBodyToFileCache(targetFile)

                        FileCacheIndex.save(
                            url.toString(),
                            cachedFile,
                            collectionProperties,
                            null,
                            requestTime,
                            context
                        )
                    }
                }
            }
            targetFile.exists() -> {
                // no network: use old version from cache (even if no cache index entry exists)
                IonLog.i("File Cache Lookup", url.toString())
                responseFromCache(targetFile, request)
            }
            else -> {
                // media file can neither be downloaded nor be found in cache
                throw FileNotAvailableException(url)
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

    // TODO Re-add info if data was retrieved from cache (before [FileStatus])
    //  check if [Response.cacheResponse] should be used here
    //  An alternative would be a custom response header
    private fun responseFromCache(targetFile: File, request: Request) = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .message("OK")
        .code(HTTP_OK)
        .body(targetFile.readBytes().toResponseBody("application/json".toMediaTypeOrNull()))
        .build()

    private fun Response.writeBodyToFileCache(targetFile: File): File {

        val file = peekBody(Long.MAX_VALUE).byteStream()
            .use { FileUtils.writeToFile(it, targetFile) }

        return file.takeIf { it.exists() }
            ?: throw IOException("Failure writing " + targetFile.path + " to local storage.")
    }
}
