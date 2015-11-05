package com.anfema.ampclient;

import android.support.annotation.NonNull;

import com.anfema.ampclient.service.AmpService;
import com.anfema.ampclient.service.models.contents.AContent;
import com.anfema.ampclient.service.models.deserializers.ContentDeserializerFactory;
import com.anfema.ampclient.service.models.deserializers.DateTimeDeserializer;
import com.anfema.ampclient.utils.Log;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Interceptor;

import org.joda.time.DateTime;

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
		final GsonConverterFactory gsonConverter = getGsonConverterFactory();


		// configure retrofit
		final Builder retrofitBuilder = new Builder();
		retrofitBuilder.addConverterFactory( gsonConverter );
		retrofitBuilder.baseUrl( baseUrl );
		Retrofit retrofit = retrofitBuilder.build();

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

	@NonNull
	private static GsonConverterFactory getGsonConverterFactory()
	{
		// configure gson converter
		final GsonBuilder gsonBuilder = new GsonBuilder();
		// parse content subtypes
		gsonBuilder.registerTypeAdapter( AContent.class, ContentDeserializerFactory.newInstance() );
		// parse datetime strings (trying two patterns)
		gsonBuilder.registerTypeAdapter( DateTime.class, new DateTimeDeserializer() );
		return GsonConverterFactory.create( gsonBuilder.create() );
	}
}
