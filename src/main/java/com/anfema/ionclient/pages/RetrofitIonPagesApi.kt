package com.anfema.ionclient.pages

import com.anfema.ionclient.pages.models.responses.CollectionResponse
import com.anfema.ionclient.pages.models.responses.PageResponse
import com.anfema.ionclient.serialization.GsonHolder
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitIonPagesApi {

    @GET("{locale}/{collection_identifier}")
    fun getCollection(
        @Path("collection_identifier") collectionIdentifier: String?,
        @Path("locale") locale: String?,
        @Query("variation") variation: String?,
        @Header("If-Modified-Since") lastModified: String?,
    ): Single<Response<CollectionResponse>>

    @GET("{locale}/{collection_identifier}/{page_identifier}")
    fun getPage(
        @Path("collection_identifier") collectionIdentifier: String?,
        @Path("page_identifier") pageIdentifier: String?,
        @Path("locale") locale: String?,
        @Query("variation") variation: String?,
    ): Single<Response<PageResponse>>
}

fun retrofitIonPagesApi(okHttpClient: OkHttpClient, baseUrl: String): RetrofitIonPagesApi =
    Retrofit.Builder()
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .addConverterFactory(GsonConverterFactory.create(GsonHolder.defaultInstance))
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .build()
        .create(RetrofitIonPagesApi::class.java)
