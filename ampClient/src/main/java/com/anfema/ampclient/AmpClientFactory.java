package com.anfema.ampclient;

import android.util.Log;

import com.anfema.ampclient.service.AmpService;
import com.anfema.ampclient.service.models.AContent;
import com.squareup.okhttp.Interceptor;

import java.util.Collection;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.Retrofit.Builder;

public class AmpClientFactory
{
	public static AmpService createClient( String baseUrl, Collection<Interceptor> interceptors )
	{
		if ( !baseUrl.endsWith( "/" ) )
		{
			baseUrl = baseUrl + "/";
			Log.i( "AmpClient", "slash was appended to base URL" );
		}

		final Builder builder = new Builder();
		builder.addConverterFactory( GsonConverterFactory.create() );
		builder.baseUrl( baseUrl );
		Retrofit retrofit = builder.build();

		if ( interceptors != null )
		{
			retrofit.client().interceptors().addAll( interceptors );
		}

		return retrofit.create( AmpService.class );
	}

	public static AmpService createClient( String baseUrl )
	{
		return createClient( baseUrl, null );
	}
}
