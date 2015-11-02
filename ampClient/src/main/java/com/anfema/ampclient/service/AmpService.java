package com.anfema.ampclient.service;

import com.anfema.ampclient.service.response_gsons.CollectionsResponse;
import com.anfema.ampclient.service.response_gsons.LoginResponse;
import com.anfema.ampclient.service.response_gsons.PagesResponse;

import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

public interface AmpService
{
	@FormUrlEncoded
	@POST("login")
	Call<LoginResponse> authenticate( @Field("username") String username, @Field("password") String password );

	@GET("collections/{collection_identifier}")
	Call<CollectionsResponse> getCollection( @Path("collection_identifier") String collectionIdentifier, @Header("Authorization") String authorizationToken );

	@GET("pages/{collection_identifier}/{page_identifier}")
	Call<PagesResponse> getPage( @Path("collection_identifier") String collectionIdentifier, @Path("page_identifier") String pageIdentifier, @Header("Authorization") String authorizationToken );
}
