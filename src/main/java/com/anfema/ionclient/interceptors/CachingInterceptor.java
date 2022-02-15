package com.anfema.ionclient.interceptors;

import android.content.Context;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.caching.FilePaths;
import com.anfema.ionclient.exceptions.NoIonPagesRequestException;
import com.anfema.ionclient.utils.ContextUtils;
import com.anfema.ionclient.utils.FileUtils;
import com.anfema.ionclient.utils.IonLog;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CachingInterceptor implements Interceptor
{
	private final IonConfig config;
	private final Context   context;

	public CachingInterceptor( IonConfig config, Context context )
	{
		this.config = config;
		this.context = ContextUtils.getApplicationContext( context );
	}

	@NonNull
	@Override
	public Response intercept( Chain chain ) throws IOException
	{
		Request request = chain.request();
		HttpUrl url = request.url();
		Response response = chain.proceed( request );

		if ( response.isSuccessful() )
		{
			// write response to cache
			String responseBody = getResponseBody( response );
			try
			{
				File filePath = FilePaths.getFilePath( url.toString(), config, context );
				FileUtils.writeTextToFile( responseBody, filePath );
			}
			catch ( NoIonPagesRequestException e )
			{
				IonLog.ex( e );
			}
		}

		return response;
	}

	/**
	 * Reads response body without closing the buffer.
	 */
	private String getResponseBody( Response response ) throws IOException
	{
		return response.peekBody( Long.MAX_VALUE ).string();
	}
}
