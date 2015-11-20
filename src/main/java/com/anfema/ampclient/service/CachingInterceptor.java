package com.anfema.ampclient.service;

import android.content.Context;

import com.anfema.ampclient.exceptions.AmpClientUnknownRequest;
import com.anfema.ampclient.caching.CacheUtils;
import com.anfema.ampclient.utils.FileUtils;
import com.anfema.ampclient.utils.Log;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import okio.Buffer;
import okio.BufferedSource;

public class CachingInterceptor implements Interceptor
{
	private static final String LOG_TAG = "AMP Caching";

	private Context appContext;

	public CachingInterceptor( Context appContext )
	{
		this.appContext = appContext;
	}

	@Override
	public Response intercept( Chain chain ) throws IOException
	{
		Request request = chain.request();
		HttpUrl url = request.httpUrl();

		try
		{
			String filePath = CacheUtils.getFilePath( url.toString(), appContext );
			if ( CacheUtils.isInCache( filePath ) )
			{
				Log.v( LOG_TAG, "Reading from cache" );
				String responseBody = FileUtils.readFromFile( filePath );
				Response cacheResponse = new Response.Builder()
						.protocol( Protocol.HTTP_1_1 )
						.request( request )
						.code( HttpURLConnection.HTTP_OK )
						.body( ResponseBody.create( MediaType.parse( com.anfema.ampclient.utils.MediaType.JSON_UTF_8.toString() ), responseBody ) )
						.build();
				return cacheResponse;
			}

			// not in cache, perform request
			Log.v( LOG_TAG, "Performing real request" );
			Response response = chain.proceed( request );

			// write response to cache
			String responseBody = getResponseBody( response );
			FileUtils.writeToFile( responseBody, filePath );

			return response;
		}

		catch ( AmpClientUnknownRequest e )
		{
			Log.e( LOG_TAG, "Skip caching and perform HTTP request" );
			Log.ex( LOG_TAG, new AmpClientUnknownRequest( url.toString() ) );
			return chain.proceed( request );
		}
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
