package com.anfema.ampclient;

import android.app.Application;
import android.content.Context;

import com.anfema.ampclient.exceptions.AuthorizationHeaderValueIsNullException;
import com.anfema.ampclient.interceptors.AuthorizationHeaderInterceptor;
import com.anfema.ampclient.interceptors.RequestLogger;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

/**
 * This class holds multiple {@link Picasso} instances.
 * <p>
 * Each instance intercepts the requests being performed by adding an authorization header – which is useful in case the link is referring to
 * the protected media repository of AMP.
 */
public class AmpPicasso
{
	/// Multiton

	// key: authorization header value
	private static Map<String, Picasso> picassoInstances = new HashMap<>();

	public static Picasso getInstance( String authorizationHeaderValue, Context context )
	{
		if ( authorizationHeaderValue == null )
		{
			throw new AuthorizationHeaderValueIsNullException();
		}

		Picasso storedPicasso = picassoInstances.get( authorizationHeaderValue );
		if ( storedPicasso != null )
		{
			return storedPicasso;
		}

		Picasso newPicasso = createPicassoInstance( authorizationHeaderValue, context );
		picassoInstances.put( authorizationHeaderValue, newPicasso );
		return newPicasso;
	}

	/// Multiton END

	/**
	 * You might want to acquire a Picasso instance by using {@link AmpPicasso#getInstance(String, Context)}, which reuses instances.
	 */
	public static Picasso createPicassoInstance( String authHeaderValue, Context context )
	{
		OkHttpClient.Builder okHttpClientBuilder = new Builder();
		okHttpClientBuilder.addInterceptor( new AuthorizationHeaderInterceptor( authHeaderValue ) );
		okHttpClientBuilder.addInterceptor( new RequestLogger( "Picasso Request" ) );
		OkHttpClient picassoClient = okHttpClientBuilder.build();

		return new Picasso.Builder( context ).downloader( new OkHttp3Downloader( picassoClient ) ).build();
	}

	/**
	 * If you have a default picasso instance you want to use with default Picasso syntax, you can set the instance.
	 * <p>
	 * Caution: if Picasso.with(this) has been called before an instance is already set and this method will throw an exception.
	 * <p>
	 * Therefore, it is recommended to call this method in {@link Application#onCreate()}. (But do not perform any long-lasting operations there.)
	 */
	public static void setupDefaultPicasso( String authHeaderValue, Context context ) throws IllegalStateException
	{
		Picasso picasso = getInstance( authHeaderValue, context );
		Picasso.setSingletonInstance( picasso );
	}
}
