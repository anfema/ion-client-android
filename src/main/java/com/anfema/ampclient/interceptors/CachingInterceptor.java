package com.anfema.ampclient.interceptors;

import android.content.Context;

import com.anfema.ampclient.caching.FilePaths;
import com.anfema.ampclient.utils.ContextUtils;
import com.anfema.ampclient.utils.FileUtils;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import okio.Buffer;
import okio.BufferedSource;

public class CachingInterceptor implements Interceptor
{
	private Context context;

	public CachingInterceptor( Context context )
	{
		this.context = ContextUtils.getApplicationContext( context );
	}

	@Override
	public Response intercept( Chain chain ) throws IOException
	{
		Request request = chain.request();
		HttpUrl url = request.httpUrl();
		//		AmpCall ampCall = AmpCall.determineCall( url );
		//
		//		// filter out all requests that shall not use caching
		//		if ( ampCall != AmpCall.COLLECTIONS && ampCall != AmpCall.PAGES )
		//		{
		//			return chain.proceed( request );
		//		}
		Response response = chain.proceed( request );

		// write response to cache
		String responseBody = getResponseBody( response );
		File filePath = FilePaths.getJsonFilePath( url.toString(), context );
		FileUtils.writeTextToFile( responseBody, filePath );

		return response;
	}

	/**
	 * Reads response body without closing the buffer.
	 *
	 * @throws IOException
	 */
	private String getResponseBody( Response response ) throws IOException
	{
		ResponseBody responseBody = response.body();
		BufferedSource source = responseBody.source();
		source.request( Long.MAX_VALUE ); // Buffer the entire body.
		Buffer buffer = source.buffer();

		Charset UTF8 = Charset.forName( "UTF-8" );
		Charset charset = UTF8;
		MediaType contentType = responseBody.contentType();
		if ( contentType != null )
		{
			charset = contentType.charset( UTF8 );
		}

		if ( responseBody.contentLength() == 0 )
		{
			return "";
		}

		return buffer.clone().readString( charset );
	}
}
