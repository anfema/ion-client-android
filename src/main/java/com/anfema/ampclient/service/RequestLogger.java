package com.anfema.ampclient.service;

import com.anfema.ampclient.utils.Log;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class RequestLogger implements Interceptor
{
	private final String logTag;

	public RequestLogger( String logTag )
	{
		this.logTag = logTag;
	}

	@Override
	public Response intercept( Chain chain ) throws IOException
	{
		Request request = chain.request();
		HttpUrl url = request.httpUrl();
		Log.i( logTag, url.toString() + " Headers: " + request.headers().toString() );
		return chain.proceed( request );
	}
}
