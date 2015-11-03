package com.anfema.ampclient;

import android.util.Log;

import com.anfema.ampclient.service.AmpService;
import com.anfema.ampclient.service.models.contents.AContent;
import com.anfema.ampclient.service.models.contents.deserializer.ContentDeserializerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
		final Gson gson = new GsonBuilder().registerTypeAdapter( AContent.class, ContentDeserializerFactory.newInstance() ).create();
		builder.addConverterFactory( GsonConverterFactory.create( gson ) );
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
