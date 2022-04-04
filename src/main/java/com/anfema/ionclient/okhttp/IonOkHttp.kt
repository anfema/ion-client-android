package com.anfema.ionclient.okhttp

import android.content.Context
import com.anfema.ionclient.IonConfig
import com.anfema.ionclient.interceptors.DeviceIdHeaderInterceptor
import com.anfema.ionclient.interceptors.IonFileCacheInterceptor
import com.anfema.ionclient.interceptors.WriteIonCacheInterceptor
import okhttp3.OkHttpClient

public fun OkHttpClient.withIonCache(config: IonConfig, context: Context): OkHttpClient =
    newBuilder()
        .cache(null)
        .addInterceptor(IonFileCacheInterceptor(config, context))
        .build()

// TODO From IonFileWithCaching
internal fun filesOkHttpClient(sharedOkHttpClient: OkHttpClient, config: IonConfig) =
    sharedOkHttpClient.newBuilder()
        .apply {
            // disable okHttp disk cache because it would store duplicate and un-used data because ION client uses its own cache
            cache(null)
        }.build()

// TODO From ApiFactory used in IonPagesWithCaching
internal fun pagesOkHttpClient(sharedOkHttpClient: OkHttpClient, config: IonConfig, context: Context) =
    sharedOkHttpClient.newBuilder()
        .apply {
            addInterceptor(DeviceIdHeaderInterceptor(context))
            addInterceptor(WriteIonCacheInterceptor(config, context))

            // disable okHttp disk cache because it would store duplicate and un-used data because ION client uses its own cache
            cache(null)
        }.build()
