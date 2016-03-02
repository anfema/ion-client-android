package com.anfema.ionclient.interceptors;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthorizationHeaderInterceptor implements Interceptor
{
	private final String authHeaderValue;

	public AuthorizationHeaderInterceptor( String authHeaderValue )
	{
		this.authHeaderValue = authHeaderValue;
	}

	@Override
	public Response intercept( Chain chain ) throws IOException
	{
		return requestWithAuthHeader( authHeaderValue, chain );
	}

	public static Response requestWithAuthHeader( String authHeaderValue, Chain chain ) throws IOException
	{
		Request newRequest = chain.request().newBuilder()
				.addHeader( "Authorization", authHeaderValue )
				.build();
		return chain.proceed( newRequest );
	}
}
