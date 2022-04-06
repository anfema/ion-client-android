package com.anfema.ionclient.mediafiles

import com.anfema.ionclient.pages.models.contents.Downloadable
import io.reactivex.Single
import okhttp3.HttpUrl
import java.io.File

internal interface IonFiles {

    fun request(content: Downloadable): Single<FileWithStatus>

    fun request(url: HttpUrl, checksum: String?): Single<FileWithStatus> =
        request(url = url, downloadUrl = url, checksum, ignoreCaching = false, targetFile = null)

    /**
     * Retrieve a file through its URL either from file cache or with a network request. The result can be cached for further requests.
     *
     * @param url           File location is defined this HTTP URL.
     * @param downloadUrl   The actual HTTP URL used to make a network request, it can differ from lookupUrl (e.g. by additional query parameter lastUpdated)
     * @param checksum      checksum of the current file on server
     * @param ignoreCaching If set to true file is retrieved through a network request and not stored in cache.
     * @param targetFile    Optionally, a custom file path can be provided. If `null`, the default scheme is used.
     *
     * @return file is retrieved when subscribed to the the result of this async operation.
     */
    fun request(
        url: HttpUrl,
        downloadUrl: HttpUrl = url,
        checksum: String?,
        ignoreCaching: Boolean = false,
        targetFile: File? = null,
    ): Single<FileWithStatus>
}
