package com.anfema.ampclient.interceptors;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

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
