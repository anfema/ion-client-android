package com.anfema.ionclient.utils

import com.anfema.ionclient.IonConfig
import com.anfema.ionclient.okhttp.okHttpClient
import com.anfema.ionclient.serialization.GsonHolder
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ApiFactory {
    @JvmStatic
    fun <T> newInstance(baseUrl: String, serviceApi: Class<T>): T {
        val okHttpClient = okHttpClient(null, IonConfig.DEFAULT_NETWORK_TIMEOUT)
        // configure retrofit
        val retrofit = Retrofit.Builder().apply {
                addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            addConverterFactory(GsonConverterFactory.create(GsonHolder.defaultInstance))
            baseUrl(baseUrl)
            client(okHttpClient)
            }.build()
        return retrofit.create(serviceApi)
    }
}
