package com.anfema.ampclient.service;

import com.anfema.ampclient.utils.Log;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class HttpLoggingInterceptor implements Interceptor
{
	private final String logTag;

	public HttpLoggingInterceptor( String logTag )
	{
		this.logTag = logTag;
	}

	@Override
	public Response intercept( Chain chain ) throws IOException
	{
		Request request = chain.request();
		HttpUrl url = request.httpUrl();
		Log.d( logTag, url.toString() + " Headers: " + request.headers().toString() );
		return chain.proceed( request );
	}
}
