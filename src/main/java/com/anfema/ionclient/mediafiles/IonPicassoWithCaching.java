package com.anfema.ionclient.mediafiles;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.anfema.ionclient.IonClient;
import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.interceptors.AuthorizationHeaderInterceptor;
import com.anfema.ionclient.interceptors.RequestLogger;
import com.anfema.ionclient.utils.Log;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * This class holds multiple {@link Picasso} instances.
 * <p>
 * Each instance intercepts the requests being performed by adding an authorization header – which is useful in case the link is referring to
 * the protected media repository of ION.
 */
public class IonPicassoWithCaching implements IonPicasso
{
	/**
	 * Global image memory cache, shared across all picasso instances
	 */
	private static LruCache picassoMemCache;

	private final IonFiles  ionFiles;
	private final Picasso   picasso;
	private       IonConfig config;

	public IonPicassoWithCaching( IonFiles ionFiles, IonConfig config, Context context )
	{
		this.ionFiles = ionFiles;
		this.config = config;
		this.picasso = createPicassoInstance( this.config::getAuthorizationHeaderValue, context );
	}

	@Override
	public void updateConfig( IonConfig config )
	{
		this.config = config;
	}

	/**
	 * You may not want to acquire a Picasso instance via {@link IonClient}.
	 */
	public static Picasso createPicassoInstance( Func0<String> authHeaderValueRetriever, Context context )
	{
		OkHttpClient.Builder okHttpClientBuilder = new Builder();
		okHttpClientBuilder.addInterceptor( new AuthorizationHeaderInterceptor( authHeaderValueRetriever ) );
		okHttpClientBuilder.addInterceptor( new RequestLogger( "Picasso Request" ) );
		OkHttpClient picassoClient = okHttpClientBuilder.build();

		return new Picasso.Builder( context )
				.downloader( new OkHttp3Downloader( picassoClient ) )
				.memoryCache( getPicassoMemoryCache( context ) )
				.build();
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
		Picasso picasso = createPicassoInstance( () -> authHeaderValue, context );
		Picasso.setSingletonInstance( picasso );
	}

	@Override
	public void loadImage( int resourceId, ImageView target, Func1<RequestCreator, RequestCreator> requestTransformation )
	{
		loadImage( resourceId, target, requestTransformation, null );
	}

	@Override
	public void loadImage( int resourceId, ImageView target, Func1<RequestCreator, RequestCreator> requestTransformation, Callback callback )
	{
		RequestCreator requestCreator = picasso.load( resourceId );
		if ( requestTransformation != null )
		{
			requestCreator = requestTransformation.call( requestCreator );
		}
		requestCreator.into( target, callback );
	}

	@Override
	public void loadImage( String path, ImageView target, Func1<RequestCreator, RequestCreator> requestTransformation )
	{
		loadImage( path, target, requestTransformation, null );
	}

	@Override
	public void loadImage( String path, ImageView target, Func1<RequestCreator, RequestCreator> requestTransformation, Callback callback )
	{
		if ( path == null || path.trim().length() == 0 )
		{
			// let picasso handle edge cases
			picasso.load( path );
			return;
		}
		loadImage( Uri.parse( path ), target, requestTransformation, callback );
	}

	@Override
	public void loadImage( Uri requestUri, ImageView target, Func1<RequestCreator, RequestCreator> requestTransformation )
	{
		loadImage( requestUri, target, requestTransformation, null );
	}

	@Override
	public void loadImage( Uri requestUri, ImageView target, Func1<RequestCreator, RequestCreator> requestTransformation, Callback callback )
	{
		Log.i( "ION Picasso", "START: requestUri: " + requestUri );
		// Log.d( "ION Picasso", "picasso instance: " + picasso + ", ion picasso instance: " + this );
		fetchImageFile( requestUri )
				.subscribe( fileUri -> showImage( fileUri, target, requestTransformation, callback ), throwable -> imageDownloadFailed( throwable, target, requestTransformation, callback ) );
	}

	private Observable<Uri> fetchImageFile( Uri uri )
	{
		if ( URLUtil.isNetworkUrl( uri.toString() ) )
		{
			HttpUrl httpUrl = HttpUrl.parse( uri.toString() );
			return ionFiles.request( httpUrl, null )
					.map( Uri::fromFile );
		}
		else
		{
			return Observable.just( uri );
		}
	}

	private void showImage( Uri uri, ImageView target, Func1<RequestCreator, RequestCreator> requestTransformation, Callback callback )
	{
		RequestCreator requestCreator = picasso.load( uri );

		// apply passed requestCreator operations
		if ( requestTransformation != null )
		{
			requestCreator = requestTransformation.call( requestCreator );
		}

		requestCreator.into( target, callback );
	}

	private void imageDownloadFailed( Throwable throwable, ImageView target, Func1<RequestCreator, RequestCreator> requestTransformation, Callback callback )
	{
		Log.ex( "ION Picasso", throwable );
		showImage( null, target, requestTransformation, null );
		if ( callback != null )
		{
			callback.onError();
		}
	}

	@Override
	public Picasso getPicassoInstance()
	{
		return picasso;
	}

	@Override
	public Observable<Picasso> getPicassoInstanceDoAuthCall()
	{
		return config.updateAuthorizationHeaderValue()
				.map( o -> picasso );
	}

	private static synchronized LruCache getPicassoMemoryCache( Context context )
	{
		if ( picassoMemCache == null )
		{
			if ( IonConfig.picassoMemCacheSize > 0 )
			{
				// use custom value
				picassoMemCache = new LruCache( IonConfig.picassoMemCacheSize );
			}
			else
			{
				// let picasso calculate cache size
				picassoMemCache = new LruCache( context );
			}
		}
		return picassoMemCache;
	}
}
