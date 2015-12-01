package com.anfema.ampclient;

import android.content.Context;

import com.anfema.ampclient.service.RequestLogger;
import com.squareup.okhttp.Interceptor.Chain;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;

public class AmpPicasso
{
	private static Map<Class<? extends AmpClientConfig>, Picasso> picassoInstances = new HashMap<>();

	public static Observable<Picasso> getInstance( Class<? extends AmpClientConfig> configClass, Context context )
	{
		if ( picassoInstances == null )
		{
			picassoInstances = new HashMap<>();
		}

		Picasso storedPicasso = picassoInstances.get( configClass );
		if ( storedPicasso != null )
		{
			return Observable.just( storedPicasso );
		}
		return TokenHolder.getToken( configClass, context )
				.map( token -> createPicassoInstance( context, token ) )
				.doOnNext( picasso -> picassoInstances.put( configClass, picasso ) );
	}

	public static Picasso createPicassoInstance( Context context, String apiToken )
	{
		OkHttpClient picassoClient = new OkHttpClient();
		picassoClient.interceptors().add( chain -> requestWithAuthHeader( apiToken, chain ) );
		picassoClient.interceptors().add( new RequestLogger( "Picasso Request" ) );

		return new Picasso.Builder( context ).downloader( new OkHttpDownloader( picassoClient ) ).build();
	}

	public static Response requestWithAuthHeader( String apiToken, Chain chain ) throws IOException
	{
		Request newRequest = chain.request().newBuilder()
				.addHeader( "Authorization", "token " + apiToken )
				.build();
		return chain.proceed( newRequest );
	}
}
