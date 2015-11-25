package com.anfema.ampclient.service;

import android.support.annotation.NonNull;

import com.anfema.ampclient.serialization.GsonFactory;
import com.anfema.ampclient.utils.FileUtils;
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
		baseUrl = ensureEndsWithSlash( baseUrl );

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

	@NonNull
	public static String ensureEndsWithSlash( String baseUrl )
	{
		if ( !baseUrl.endsWith( FileUtils.SLASH ) )
		{
			baseUrl = baseUrl + FileUtils.SLASH;
			Log.i( "AmpClient", "slash was appended to base URL" );
		}
		return baseUrl;
	}

	public static <T> T newInstance( String baseUrl, Class<T> serviceApi )
	{
		return newInstance( baseUrl, null, serviceApi );
	}
}
