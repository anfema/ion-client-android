package com.anfema.ampclient.service;

import com.anfema.ampclient.serialization.GsonFactory;
import com.anfema.ampclient.utils.Log;
import com.squareup.okhttp.Interceptor;

import java.util.Collection;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.Retrofit.Builder;
import retrofit.RxJavaCallAdapterFactory;

public class AmpApiFactory
{
	public static <T> T newInstance( String baseUrl, Collection<Interceptor> interceptors, Class<T> serviceApi )
	{
		if ( !baseUrl.endsWith( "/" ) )
		{
			baseUrl = baseUrl + "/";
			Log.i( "AmpClient", "slash was appended to base URL" );
		}

		// configure retrofit
		final Builder retrofitBuilder = new Builder();
		retrofitBuilder.addCallAdapterFactory( RxJavaCallAdapterFactory.create() ); // enable returning Observables
		retrofitBuilder.addConverterFactory( GsonConverterFactory.create( GsonFactory.newInstance() ) );
		retrofitBuilder.baseUrl( baseUrl );
		Retrofit retrofit = retrofitBuilder.build();

		if ( interceptors != null )
		{
			retrofit.client().interceptors().addAll( interceptors );
		}

		return retrofit.create( serviceApi );
	}

	public static <T> T newInstance( String baseUrl, Class<T> serviceApi )
	{
		return newInstance( baseUrl, null, serviceApi );
	}
}
