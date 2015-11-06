package com.anfema.ampclient.service;

import com.anfema.ampclient.service.models.CollectionsResponse;
import com.anfema.ampclient.service.models.LoginResponse;
import com.anfema.ampclient.service.models.PagesResponse;

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
	Call<CollectionsResponse> getCollection( @Path("collection_identifier") String collectionIdentifier, @Header("Authorization") String authorizationToken );

	@GET("collections/{collection_identifier}")
	Observable<CollectionsResponse> getCollection2( @Path("collection_identifier") String collectionIdentifier, @Header("Authorization") String authorizationToken );

	@GET("pages/{collection_identifier}/{page_identifier}")
	Call<PagesResponse> getPage( @Path("collection_identifier") String collectionIdentifier, @Path("page_identifier") String pageIdentifier, @Header("Authorization") String authorizationToken );
}
