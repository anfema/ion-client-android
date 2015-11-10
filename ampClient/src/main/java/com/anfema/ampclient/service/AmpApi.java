package com.anfema.ampclient.service;

import com.anfema.ampclient.models.responses.CollectionResponse;
import com.anfema.ampclient.models.responses.LoginResponse;
import com.anfema.ampclient.models.responses.PageResponse;

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
	// methods using reactive X pattern returning observable

	@FormUrlEncoded
	@POST("login")
	Observable<LoginResponse> authenticate( @Field("username") String username, @Field("password") String password );

	@GET("collections/{collection_identifier}")
	Observable<CollectionResponse> getCollection( @Path("collection_identifier") String collectionIdentifier, @Header("Authorization") String authorizationToken );

	@GET("pages/{collection_identifier}/{page_identifier}")
	Observable<PageResponse> getPage( @Path("collection_identifier") String collectionIdentifier, @Path("page_identifier") String pageIdentifier, @Header("Authorization") String authorizationToken );


	// methods using conventional return value to be called synchronously or by passing a {@link retrofit.Callback}

	@FormUrlEncoded
	@POST("login")
	Call<LoginResponse> authenticateConventional( @Field("username") String username, @Field("password") String password );

	@GET("collections/{collection_identifier}")
	Call<CollectionResponse> getCollectionConventional( @Path("collection_identifier") String collectionIdentifier, @Header("Authorization") String authorizationToken );

	@GET("pages/{collection_identifier}/{page_identifier}")
	Call<PageResponse> getPageConventional( @Path("collection_identifier") String collectionIdentifier, @Path("page_identifier") String pageIdentifier, @Header("Authorization") String authorizationToken );
}
