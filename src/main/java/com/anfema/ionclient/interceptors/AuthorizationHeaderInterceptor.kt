package com.anfema.ionclient.interceptors

import com.anfema.ionclient.utils.IonLog
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Adds header 'Authorization' to request if a value is provided.
 */
class AuthorizationHeaderInterceptor(
    private val authHeaderValueProvider: () -> String?,
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val authHeaderValue = authHeaderValueProvider()
        val newRequest = chain.request().newBuilder().apply {
            if (authHeaderValue != null) {
                addHeader("Authorization", authHeaderValue)
            } else {
                IonLog.e(
                    javaClass.simpleName,
                    "No Authorization header was added to request ${chain.request().url}"
                )
            }
        }.build()
        return chain.proceed(newRequest)
    }
}
