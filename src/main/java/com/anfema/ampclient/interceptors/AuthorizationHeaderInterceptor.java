package com.anfema.ampclient.interceptors;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class AuthorizationHeaderInterceptor implements Interceptor
{
	private final String apiToken;

	public AuthorizationHeaderInterceptor( String apiToken )
	{
		this.apiToken = apiToken;
	}

	@Override
	public Response intercept( Chain chain ) throws IOException
	{
		return requestWithAuthHeader( apiToken, chain );
	}

	public static Response requestWithAuthHeader( String apiToken, Chain chain ) throws IOException
	{
		Request newRequest = chain.request().newBuilder()
				.addHeader( "Authorization", "token " + apiToken )
				.build();
		return chain.proceed( newRequest );
	}
}
