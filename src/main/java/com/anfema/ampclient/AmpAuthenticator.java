package com.anfema.ampclient;

import com.anfema.ampclient.models.responses.LoginResponse;
import com.anfema.ampclient.service.AmpApiFactory;
import com.anfema.ampclient.service.AmpApiRx;
import com.anfema.ampclient.utils.RxUtils;

import rx.Observable;

/**
 * Use AMPs internal login request and retrieve API token.
 */
public class AmpAuthenticator
{
	public static Observable<String> requestApiToken( String baseUrl, String username, String password )
	{
		AmpApiRx ampApi = AmpApiFactory.newInstance( baseUrl, AmpApiRx.class );
		return ampApi.authenticate( username, password )
				.map( LoginResponse::getToken )
				.doOnError( RxUtils.DEFAULT_EXCEPTION_HANDLER )
				.compose( RxUtils.applySchedulers() );
	}
}
