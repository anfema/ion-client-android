package com.anfema.ionclient.okhttp

import android.content.Context
import com.anfema.ionclient.IonConfig
import com.anfema.ionclient.interceptors.AdditionalHeadersInterceptor
import com.anfema.ionclient.interceptors.AuthorizationHeaderInterceptor
import com.anfema.ionclient.interceptors.IonFileCacheInterceptor
import com.anfema.ionclient.utils.IonLog
import com.anfema.utils.NetworkUtils
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

public fun OkHttpClient.withIonCache(config: IonConfig, context: Context): OkHttpClient =
    newBuilder()
        .cache(null)
        .addInterceptor(IonFileCacheInterceptor(config, context))
        .build()

// TODO From IonFileWithCaching
fun okHttpClient(
    authorizationHeaderValue: Function0<String?>,
    additionalHeaders: Map<String, String> = emptyMap(),
    networkTimeout: Int,
): OkHttpClient {
    val okHttpClientBuilder = OkHttpClient.Builder()
    NetworkUtils.applyTimeout(okHttpClientBuilder, networkTimeout)
    okHttpClientBuilder.addInterceptor(AuthorizationHeaderInterceptor(authorizationHeaderValue))
    okHttpClientBuilder.addInterceptor(AdditionalHeadersInterceptor(additionalHeaders))
    if (IonConfig.logLevel <= IonLog.INFO && IonConfig.logLevel >= IonLog.VERBOSE) {
        okHttpClientBuilder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
    }
    // disable okHttp disk cache because it would store duplicate and un-used data because ION client uses its own cache
    okHttpClientBuilder.cache(null)
    return okHttpClientBuilder.build()
}

// TODO From ApiFactory used in IonPagesWithCaching
fun okHttpClient(
    interceptors: Collection<Interceptor>?,
    networkTimeout: Int,
): OkHttpClient =
    OkHttpClient.Builder().apply {
        NetworkUtils.applyTimeout(this, networkTimeout)
        interceptors?.let {
            for (interceptor in it) {
                addInterceptor(interceptor)
            }
        }
    }.build()
