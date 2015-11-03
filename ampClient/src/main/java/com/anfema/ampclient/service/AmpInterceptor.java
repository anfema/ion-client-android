package com.anfema.ampclient.service;


import android.util.Log;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class AmpInterceptor implements com.squareup.okhttp.Interceptor
{
	@Override
	public Response intercept( Chain chain ) throws IOException
	{
		Request request = chain.request();

		Log.i( "Interceptor", "before request" );

		// processing request and interception on response did not work with retrofit 2.0.0-beta2
		// Response response = chain.proceed( request );
		// Log.i( "Interceptor", "after request" );
		// return response;

		return chain.proceed( request );
	}
}
