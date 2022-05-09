package com.anfema.ionclient.interceptors

import android.content.Context
import com.anfema.ionclient.CollectionProperties
import com.anfema.ionclient.caching.FilePaths
import com.anfema.ionclient.exceptions.NoIonPagesRequestException
import com.anfema.ionclient.utils.FileUtils
import com.anfema.ionclient.utils.IonLog
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Writes responses to the file cache
 * but does not create a cache index entry!
 * That's bad coherence/encapsulation.
 */
class WriteIonCacheInterceptor(
    private val collectionProperties: CollectionProperties,
    private val context: Context,
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val url = request.url

        val response: Response = chain.proceed(request)

        if (response.isSuccessful) {

            // write response to cache
            val responseBody = response.peekBody(Long.MAX_VALUE)

            try {
                val filePath = FilePaths.getFilePath(url.toString(), collectionProperties, context)
                FileUtils.writeToFile(responseBody.byteStream(), filePath)
            } catch (e: NoIonPagesRequestException) {
                IonLog.ex(e)
            }
        }
        return response
    }
}
