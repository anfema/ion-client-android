package com.anfema.ionclient.okhttp

import android.content.Context
import com.anfema.ionclient.IonConfig
import com.anfema.ionclient.interceptors.AdditionalHeadersInterceptor
import com.anfema.ionclient.interceptors.AuthorizationHeaderInterceptor
import com.anfema.ionclient.interceptors.DeviceIdHeaderInterceptor
import com.anfema.ionclient.interceptors.IonFileCacheInterceptor
import com.anfema.ionclient.interceptors.WriteIonCacheInterceptor
import com.anfema.ionclient.utils.IonLog
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

public fun OkHttpClient.withIonCache(config: IonConfig, context: Context): OkHttpClient =
    newBuilder()
        .cache(null)
        .addInterceptor(IonFileCacheInterceptor(config, context))
        .build()

// TODO From IonFileWithCaching
fun filesOkHttpClient(sharedOkHttpClient: OkHttpClient, config: IonConfig) =
    sharedOkHttpClient.newBuilder()
        .apply {
            withTimeout(config.networkTimeout)

            addInterceptor(AuthorizationHeaderInterceptor { config.authorizationHeaderValue })
            addInterceptor(AdditionalHeadersInterceptor(config.additionalHeaders))
            if (IonConfig.logLevel <= IonLog.INFO && IonConfig.logLevel >= IonLog.VERBOSE) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
            // disable okHttp disk cache because it would store duplicate and un-used data because ION client uses its own cache
            cache(null)
        }.build()

// TODO From ApiFactory used in IonPagesWithCaching
fun pagesOkHttpClient(sharedOkHttpClient: OkHttpClient, config: IonConfig, context: Context) =
    sharedOkHttpClient.newBuilder().apply {
        withTimeout(config.networkTimeout)

        val ionPageInterceptors = buildList {
            add(DeviceIdHeaderInterceptor(context))
            add(WriteIonCacheInterceptor(config, context))
            add(AdditionalHeadersInterceptor(config.additionalHeaders))
            if (IonConfig.logLevel <= IonLog.INFO && IonConfig.logLevel >= IonLog.VERBOSE) {
                add(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            }
        }

        ionPageInterceptors.forEach {
            addInterceptor(it)
        }
    }.build()

fun OkHttpClient.Builder.withTimeout(networkTimeout: Int) =
    apply {
        if (networkTimeout > 0) {
            connectTimeout(networkTimeout.toLong(), TimeUnit.SECONDS)
            readTimeout(networkTimeout.toLong(), TimeUnit.SECONDS)
            writeTimeout(networkTimeout.toLong(), TimeUnit.SECONDS)
        }
    }
