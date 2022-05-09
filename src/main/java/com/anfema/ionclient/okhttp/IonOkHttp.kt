package com.anfema.ionclient.okhttp

import android.content.Context
import com.anfema.ionclient.CollectionProperties
import com.anfema.ionclient.interceptors.IonFileCacheInterceptor
import com.anfema.ionclient.interceptors.WriteIonCacheInterceptor
import okhttp3.OkHttpClient

/**
 * Use this to configure an OkHttpClient to use the ION cache, where the ION API can't be accessed
 * via an [com.anfema.ionclient.IonClient] instance, e.g. when using an image loader or video player.
 */
fun OkHttpClient.withIonFileCache(collectionProperties: CollectionProperties, context: Context): OkHttpClient =
    newBuilder()
        .cache(null)
        .addInterceptor(IonFileCacheInterceptor(collectionProperties, context))
        .build()

internal fun filesOkHttpClient(sharedOkHttpClient: OkHttpClient, collectionProperties: CollectionProperties) =
    sharedOkHttpClient.newBuilder()
        .apply {
            // TODO Use IonFilesWithCaching but also pass checksum via header

            // disable okHttp disk cache because it would store duplicate and un-used data because ION client uses its own cache
            cache(null)
        }.build()

internal fun pagesOkHttpClient(sharedOkHttpClient: OkHttpClient, collectionProperties: CollectionProperties, context: Context) =
    sharedOkHttpClient.newBuilder()
        .apply {
            addInterceptor(WriteIonCacheInterceptor(collectionProperties, context))

            // disable okHttp disk cache because it would store duplicate and un-used data because ION client uses its own cache
            cache(null)
        }.build()
