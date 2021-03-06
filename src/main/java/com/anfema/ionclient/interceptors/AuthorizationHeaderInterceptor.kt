package com.anfema.ionclient.interceptors

import com.anfema.ionclient.utils.IonLog
import com.anfema.utils.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.Callable

/**
 * Add header 'Authorization' to request.
 */
class AuthorizationHeaderInterceptor : Interceptor {
    private val authHeaderValue: String?
    private val authHeaderValueRetriever: Callable<String>?

    /**
     * Provide value for 'Authorization' header directly.
     */
    constructor(authHeaderValue: String?) {
        this.authHeaderValue = authHeaderValue
        authHeaderValueRetriever = null
    }

    /**
     * Provide value for 'Authorization' header through the return value of a function.
     */
    constructor(authHeaderValueRetriever: Callable<String>?) {
        authHeaderValue = null
        this.authHeaderValueRetriever = authHeaderValueRetriever
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val authHeaderValue = authHeaderValue ?: try {
            authHeaderValueRetriever?.call()
        } catch (e: Exception) {
            Log.ex(e)
            null
        }
        val newRequest = chain.request().newBuilder().apply {
            if (authHeaderValue != null) {
                addHeader("Authorization", authHeaderValue)
            } else {
                IonLog.e(
                    javaClass.simpleName,
                    "No Authorization header was added to request ${chain.request().url()}"
                )
            }
        }.build()
        return chain.proceed(newRequest)
    }
}
