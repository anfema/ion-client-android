package com.anfema.ionclient.authentication;

import com.anfema.ionclient.authentication.models.LoginResponse;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * methods using reactive X pattern returning {@link Observable}
 */
public interface IonLoginApi
{
	@FormUrlEncoded
	@POST("login")
	Observable<LoginResponse> login( @Field("username") String username, @Field("password") String password );
}
