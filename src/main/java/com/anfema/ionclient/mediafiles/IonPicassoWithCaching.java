package com.anfema.ionclient.mediafiles;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.anfema.ionclient.IonClient;
import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.interceptors.AuthorizationHeaderInterceptor;
import com.anfema.ionclient.interceptors.RequestLogger;
import com.anfema.ionclient.pages.models.contents.Downloadable;
import com.anfema.ionclient.utils.IonLog;
import com.anfema.utils.Log;
import com.anfema.utils.NetworkUtils;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

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
		this.picasso = createPicassoInstance( this.config::getAuthorizationHeaderValue, context, config.networkTimeout );
	}

	@Override
	public void updateConfig( IonConfig config )
	{
		this.config = config;
	}

	/**
	 * You may not want to acquire a Picasso instance via {@link IonClient}.
	 */
	public static Picasso createPicassoInstance( Callable<String> authHeaderValueRetriever, Context context, int networkTimeout )
	{
		OkHttpClient.Builder okHttpClientBuilder = new Builder();
		NetworkUtils.applyTimeout( okHttpClientBuilder, networkTimeout );
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
	public static void setupDefaultPicasso( String authHeaderValue, Context context, int networkTimeout ) throws IllegalStateException
	{
		Picasso picasso = createPicassoInstance( () -> authHeaderValue, context, networkTimeout );
		Picasso.setSingletonInstance( picasso );
	}

	@Override
	public void loadImage( int resourceId, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation )
	{
		loadImage( resourceId, target, requestTransformation, null );
	}

	@Override
	public void loadImage( int resourceId, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation, Callback callback )
	{
		RequestCreator requestCreator = picasso.load( resourceId );
		if ( requestTransformation != null )
		{
			try
			{
				requestCreator = requestTransformation.apply( requestCreator );
			}
			catch ( Exception e )
			{
				Log.ex( e );
			}
		}
		requestCreator.into( target, callback );
	}

	@Override
	public void loadImage( String path, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation )
	{
		loadImage( path, target, requestTransformation, null );
	}

	@Override
	public void loadImage( String path, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation, Callback callback )
	{
		loadImage( path, null, target, requestTransformation, callback );
	}

	@Override
	public void loadImage( Downloadable image, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation )
	{

		loadImage( image, target, requestTransformation, null );
	}

	@Override
	public void loadImage( Downloadable image, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation, Callback callback )
	{
		String path = image != null ? image.getUrl() : null;
		String checksum = image != null ? image.getChecksum() : null;
		loadImage( path, checksum, target, requestTransformation, callback );
	}

	private void loadImage( String path, String checksum, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation, Callback callback )
	{
		IonLog.i( "ION Picasso", "START: requestUri: " + path + ", checksum: " + checksum );

		if ( path == null || path.trim().length() == 0 )
		{
			// let picasso handle edge cases
			RequestCreator requestCreator = picasso.load( path );
			if ( requestTransformation != null )
			{
				try
				{
					requestCreator = requestTransformation.apply( requestCreator );
				}
				catch ( Exception e )
				{
					Log.ex( e );
				}
			}
			requestCreator.into( target, callback );
			return;
		}

		fetchImageFile( Uri.parse( path ), checksum )
				.subscribe( fileUri -> showImage( fileUri, target, requestTransformation, callback ), throwable -> imageDownloadFailed( throwable, target, requestTransformation, callback ) );
	}

	private Single<Uri> fetchImageFile( @NonNull Uri uri, @Nullable String checksum )
	{
		if ( URLUtil.isNetworkUrl( uri.toString() ) )
		{
			HttpUrl httpUrl = HttpUrl.parse( uri.toString() );
			return ionFiles.request( httpUrl, checksum )
					.map( Uri::fromFile );
		}
		else
		{
			return Single.just( uri );
		}
	}

	private void showImage( Uri uri, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation, Callback callback )
	{
		RequestCreator requestCreator = picasso.load( uri );

		// apply passed requestCreator operations
		if ( requestTransformation != null )
		{
			try
			{
				requestCreator = requestTransformation.apply( requestCreator );
			}
			catch ( Exception e )
			{
				Log.ex( e );
			}
		}

		requestCreator.into( target, callback );
	}

	private void imageDownloadFailed( Throwable throwable, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation, Callback callback )
	{
		IonLog.ex( "ION Picasso", throwable );
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
	public Single<Picasso> getPicassoInstanceDoAuthCall()
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
