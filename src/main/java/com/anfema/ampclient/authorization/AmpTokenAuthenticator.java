package com.anfema.ampclient.authorization;

import com.anfema.ampclient.models.responses.LoginResponse;
import com.anfema.ampclient.service.AmpApiFactory;
import com.anfema.ampclient.service.AmpApiRx;
import com.anfema.ampclient.utils.RxUtils;

import rx.Observable;

/**
 * Use AMPs internal login request and retrieve API token.
 */
public class AmpTokenAuthenticator
{
	public static Observable<String> requestAuthHeaderValue( String baseUrl, String username, String password )
	{
		AmpApiRx ampApi = AmpApiFactory.newInstance( baseUrl, AmpApiRx.class );
		return ampApi.login( username, password )
				.map( LoginResponse::getToken )
				.map( TokenAuth::getAuthHeaderValue )
				.doOnError( RxUtils.DEFAULT_EXCEPTION_HANDLER )
				.compose( RxUtils.runOnIoThread() );
	}
}
