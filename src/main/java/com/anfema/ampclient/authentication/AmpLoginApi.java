package com.anfema.ampclient.authentication;

import com.anfema.ampclient.authentication.models.LoginResponse;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * methods using reactive X pattern returning {@link Observable}
 */
public interface AmpLoginApi
{
	@FormUrlEncoded
	@POST("login")
	Observable<LoginResponse> login( @Field("username") String username, @Field("password") String password );
}
