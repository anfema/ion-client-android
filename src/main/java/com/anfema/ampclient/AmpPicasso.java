package com.anfema.ampclient;

import android.content.Context;

import com.anfema.ampclient.authorization.AuthorizationHolder;
import com.anfema.ampclient.interceptors.AuthorizationHeaderInterceptor;
import com.anfema.ampclient.interceptors.RequestLogger;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

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
		return AuthorizationHolder.getAuthHeaderValue( configClass, context )
				.map( authHeaderValue -> createPicassoInstance( context, authHeaderValue ) )
				.doOnNext( picasso -> picassoInstances.put( configClass, picasso ) );
	}

	public static Picasso createPicassoInstance( Context context, String authHeaderValue )
	{
		OkHttpClient picassoClient = new OkHttpClient();
		picassoClient.interceptors().add( new AuthorizationHeaderInterceptor( authHeaderValue ) );
		picassoClient.interceptors().add( new RequestLogger( "Picasso Request" ) );

		return new Picasso.Builder( context ).downloader( new OkHttpDownloader( picassoClient ) ).build();
	}
}
