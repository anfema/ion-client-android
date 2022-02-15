package com.anfema.ionclient.interceptors

import android.content.Context
import com.anfema.ionclient.IonConfig
import com.anfema.ionclient.caching.FilePaths
import com.anfema.ionclient.exceptions.NoIonPagesRequestException
import com.anfema.ionclient.utils.FileUtils
import com.anfema.ionclient.utils.IonLog
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class CachingInterceptor(
    private val config: IonConfig,
    private val context: Context,
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val url = request.url

        val response: Response = chain.proceed(request)

        if (response.isSuccessful) {

            // write response to cache
            val responseBody = getResponseBody(response)
            try {
                val filePath = FilePaths.getFilePath(url.toString(), config, context)
                FileUtils.writeTextToFile(responseBody, filePath)
            } catch (e: NoIonPagesRequestException) {
                IonLog.ex(e)
            }
        }
        return response
    }

    /**
     * Reads response body without closing the buffer.
     */
    @Throws(IOException::class)
    private fun getResponseBody(response: Response): String {
        return response.peekBody(Long.MAX_VALUE).string()
    }
}
