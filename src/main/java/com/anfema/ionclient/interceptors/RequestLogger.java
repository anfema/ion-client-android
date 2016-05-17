package com.anfema.ionclient.interceptors;

import com.anfema.ionclient.utils.IonLog;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

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
		HttpUrl url = request.url();
		IonLog.i( logTag, url.toString() + " Headers: " + request.headers().toString() );
		return chain.proceed( request );
	}
}
