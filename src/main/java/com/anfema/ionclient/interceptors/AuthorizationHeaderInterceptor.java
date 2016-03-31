package com.anfema.ionclient.interceptors;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import rx.functions.Func0;

/**
 * Add header 'Authorization' to request.
 */
public class AuthorizationHeaderInterceptor implements Interceptor
{
	private final String        authHeaderValue;
	private final Func0<String> authHeaderValueRetriever;

	/**
	 * Provide value for 'Authorization' header directly.
	 */
	public AuthorizationHeaderInterceptor( String authHeaderValue )
	{
		this.authHeaderValue = authHeaderValue;
		this.authHeaderValueRetriever = null;
	}

	/**
	 * Provide value for 'Authorization' header through the return value of a function.
	 */
	public AuthorizationHeaderInterceptor( Func0<String> authHeaderValueRetriever )
	{
		this.authHeaderValue = null;
		this.authHeaderValueRetriever = authHeaderValueRetriever;
	}

	@Override
	public Response intercept( Chain chain ) throws IOException
	{
		String authHeaderValue = this.authHeaderValue;
		if ( authHeaderValue == null && authHeaderValueRetriever != null )
		{
			authHeaderValue = authHeaderValueRetriever.call();
		}
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
