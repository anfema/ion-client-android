package com.anfema.ionclient.pages

import com.anfema.ionclient.pages.models.responses.CollectionResponse
import com.anfema.ionclient.pages.models.responses.PageResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface IonPagesApi {

    @GET("{locale}/{collection_identifier}")
    fun getCollection(
        @Path("collection_identifier") collectionIdentifier: String?,
        @Path("locale") locale: String?,
        @Header("Authorization") authorizationToken: String?,
        @Query("variation") variation: String?,
        @Header("If-Modified-Since") lastModified: String?,
    ): Single<Response<CollectionResponse>>

    @GET("{locale}/{collection_identifier}/{page_identifier}")
    fun getPage(
        @Path("collection_identifier") collectionIdentifier: String?,
        @Path("page_identifier") pageIdentifier: String?,
        @Path("locale") locale: String?,
        @Query("variation") variation: String?,
        @Header("Authorization") authorizationToken: String?,
    ): Single<Response<PageResponse>>
}
