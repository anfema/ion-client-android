package com.anfema.ionclient.utils

import com.anfema.ionclient.IonConfig
import com.anfema.ionclient.serialization.GsonHolder
import com.anfema.utils.NetworkUtils
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ApiFactory {
    @JvmStatic
    fun <T> newInstance(baseUrl: String, serviceApi: Class<T>): T =
        newInstance(baseUrl, null, serviceApi, IonConfig.DEFAULT_NETWORK_TIMEOUT)

    @JvmStatic
    fun <T> newInstance(
        baseUrl: String,
        interceptors: Collection<Interceptor>?,
        serviceApi: Class<T>?,
        networkTimeout: Int,
    ): T {
        val okHttpClient = okHttpClient(interceptors, networkTimeout)

        // configure retrofit
        val retrofit = Retrofit.Builder().apply {
            addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            addConverterFactory(GsonConverterFactory.create(GsonHolder.getInstance()))
            baseUrl(ensureEndsWithSlash(baseUrl))
            client(okHttpClient)
        }.build()
        return retrofit.create(serviceApi)
    }

    private fun okHttpClient(interceptors: Collection<Interceptor>?, networkTimeout: Int) =
        OkHttpClient.Builder().apply {
            NetworkUtils.applyTimeout(this, networkTimeout)
            interceptors?.let {
                for (interceptor in it) {
                    addInterceptor(interceptor)
                }
            }
        }.build()

    private fun ensureEndsWithSlash(baseUrl: String) =
        if (!baseUrl.endsWith(FileUtils.SLASH)) {
            IonLog.i("API factory", "slash was appended to base URL")
            baseUrl + FileUtils.SLASH
        } else {
            baseUrl
        }
}
