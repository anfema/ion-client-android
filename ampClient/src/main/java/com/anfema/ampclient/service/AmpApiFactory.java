package com.anfema.ampclient.service;

import android.support.annotation.NonNull;

import com.anfema.ampclient.models.contents.AContent;
import com.anfema.ampclient.models.deserializers.ContentDeserializerFactory;
import com.anfema.ampclient.models.deserializers.DateTimeDeserializer;
import com.anfema.ampclient.utils.Log;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Interceptor;

import org.joda.time.DateTime;

import java.util.Collection;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.Retrofit.Builder;
import retrofit.RxJavaCallAdapterFactory;

public class AmpApiFactory
{
	public static AmpApi newInstance( String baseUrl, Collection<Interceptor> interceptors )
	{
		if ( !baseUrl.endsWith( "/" ) )
		{
			baseUrl = baseUrl + "/";
			Log.i( "AmpClient", "slash was appended to base URL" );
		}

		// configure retrofit
		final Builder retrofitBuilder = new Builder();
		retrofitBuilder.addCallAdapterFactory( RxJavaCallAdapterFactory.create() ); // enable returning Observables
		retrofitBuilder.addConverterFactory( getGsonConverterFactory() );
		retrofitBuilder.baseUrl( baseUrl );
		Retrofit retrofit = retrofitBuilder.build();

		if ( interceptors != null )
		{
			retrofit.client().interceptors().addAll( interceptors );
		}

		return retrofit.create( AmpApi.class );
	}

	public static AmpApi newInstance( String baseUrl )
	{
		return newInstance( baseUrl, null );
	}

	@NonNull
	private static GsonConverterFactory getGsonConverterFactory()
	{
		final GsonBuilder gsonBuilder = new GsonBuilder();
		// parse content subtypes
		gsonBuilder.registerTypeAdapter( AContent.class, ContentDeserializerFactory.newInstance() );
		// parse datetime strings (trying two patterns)
		gsonBuilder.registerTypeAdapter( DateTime.class, new DateTimeDeserializer() );
		return GsonConverterFactory.create( gsonBuilder.create() );
	}
}
