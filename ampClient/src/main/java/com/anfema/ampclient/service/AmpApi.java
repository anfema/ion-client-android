package com.anfema.ampclient.service;

import com.anfema.ampclient.service.models.CollectionResponse;
import com.anfema.ampclient.service.models.LoginResponse;
import com.anfema.ampclient.service.models.PageResponse;

import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import rx.Observable;

public interface AmpApi
{
	@FormUrlEncoded
	@POST("login")
	Call<LoginResponse> authenticate( @Field("username") String username, @Field("password") String password );

	@GET("collections/{collection_identifier}")
	Call<CollectionResponse> getCollection( @Path("collection_identifier") String collectionIdentifier, @Header("Authorization") String authorizationToken );

	@GET("pages/{collection_identifier}/{page_identifier}")
	Call<PageResponse> getPage( @Path("collection_identifier") String collectionIdentifier, @Path("page_identifier") String pageIdentifier, @Header("Authorization") String authorizationToken );

	@GET("collections/{collection_identifier}")
	Observable<CollectionResponse> getCollectionRx( @Path("collection_identifier") String collectionIdentifier, @Header("Authorization") String authorizationToken );

	@GET("pages/{collection_identifier}/{page_identifier}")
	Observable<PageResponse> getPageRx( @Path("collection_identifier") String collectionIdentifier, @Path("page_identifier") String pageIdentifier, @Header("Authorization") String authorizationToken );
}
