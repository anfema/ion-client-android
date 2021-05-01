package com.anfema.ionclient.utils;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.serialization.GsonHolder;
import com.anfema.utils.NetworkUtils;

import java.util.Collection;

import androidx.annotation.NonNull;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiFactory
{
	public static <T> T newInstance( String baseUrl, Collection<Interceptor> interceptors, Class<T> serviceApi, int networkTimeout )
	{
		OkHttpClient okHttpClient = okHttpClient( interceptors, networkTimeout );

		// configure retrofit
		final Builder retrofitBuilder = new Builder();
		retrofitBuilder.addCallAdapterFactory( RxJava2CallAdapterFactory.createWithScheduler( Schedulers.io() ) );
		retrofitBuilder.addConverterFactory( GsonConverterFactory.create( GsonHolder.getInstance() ) );
		retrofitBuilder.baseUrl( ensureEndsWithSlash( baseUrl ) );
		retrofitBuilder.client( okHttpClient );
		Retrofit retrofit = retrofitBuilder.build();

		return retrofit.create( serviceApi );
	}

	private static OkHttpClient okHttpClient( Collection<Interceptor> interceptors, int networkTimeout )
	{
		// configure okHttp client: add interceptors
		OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
		NetworkUtils.applyTimeout( okHttpClientBuilder, networkTimeout );
		if ( interceptors != null )
		{
			for ( Interceptor interceptor : interceptors )
			{
				okHttpClientBuilder.addInterceptor( interceptor );
			}
		}
		return okHttpClientBuilder.build();
	}

	@NonNull
	private static String ensureEndsWithSlash( String baseUrl )
	{
		if ( !baseUrl.endsWith( FileUtils.SLASH ) )
		{
			baseUrl = baseUrl + FileUtils.SLASH;
			IonLog.i( "API factory", "slash was appended to base URL" );
		}
		return baseUrl;
	}

	public static <T> T newInstance( String baseUrl, Class<T> serviceApi )
	{
		return newInstance( baseUrl, null, serviceApi, IonConfig.DEFAULT_NETWORK_TIMEOUT );
	}
}
