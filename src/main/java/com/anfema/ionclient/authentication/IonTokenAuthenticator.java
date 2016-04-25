package com.anfema.ionclient.authentication;

import com.anfema.ionclient.authentication.models.LoginResponse;
import com.anfema.ionclient.utils.ApiFactory;

import rx.Observable;

/**
 * Use IONs internal login request and retrieve API token.
 */
public class IonTokenAuthenticator
{
	public static Observable<String> requestAuthHeaderValue( String baseUrl, String username, String password )
	{
		IonLoginApi ionApi = ApiFactory.newInstance( baseUrl, IonLoginApi.class );
		return ionApi.login( username, password )
				.map( LoginResponse::getToken )
				.map( TokenAuth::getAuthHeaderValue );
	}
}
