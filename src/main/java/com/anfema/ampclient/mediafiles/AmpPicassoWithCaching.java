package com.anfema.ampclient.mediafiles;

import android.app.Application;
import android.content.Context;
import android.net.Uri;

import com.anfema.ampclient.interceptors.AuthorizationHeaderInterceptor;
import com.anfema.ampclient.interceptors.RequestLogger;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

/**
 * This class holds multiple {@link Picasso} instances.
 * <p/>
 * Each instance intercepts the requests being performed by adding an authorization header – which is useful in case the link is referring to
 * the protected media repository of AMP.
 */
public class AmpPicassoWithCaching implements AmpPicasso
{
	private final AmpFiles ampFiles;
	private final Picasso  picasso;

	public AmpPicassoWithCaching( AmpFiles ampFiles, String authHeaderValue, Context context )
	{
		this.ampFiles = ampFiles;
		this.picasso = createPicassoInstance( authHeaderValue, context );
	}

	/**
	 * You may not want to acquire a Picasso instance via {@link com.anfema.ampclient.AmpClient}.
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
	 * <p/>
	 * Caution: if Picasso.with(this) has been called before an instance is already set and this method will throw an exception.
	 * <p/>
	 * Therefore, it is recommended to call this method in {@link Application#onCreate()}. (But do not perform any long-lasting operations there.)
	 */
	public static void setupDefaultPicasso( String authHeaderValue, Context context ) throws IllegalStateException
	{
		Picasso picasso = createPicassoInstance( authHeaderValue, context );
		Picasso.setSingletonInstance( picasso );
	}

	@Override
	public RequestCreator loadImage( String path )
	{
		return picasso.load( path );
	}

	@Override
	public RequestCreator loadImage( Uri uri )
	{
		return picasso.load( uri );
	}

	@Override
	public RequestCreator loadImage( int resourceID )
	{
		return picasso.load( resourceID );
	}

	@Override
	public Picasso getPicassoInstance()
	{
		return picasso;
	}
}
