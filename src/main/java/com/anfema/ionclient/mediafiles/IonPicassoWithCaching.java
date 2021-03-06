package com.anfema.ionclient.mediafiles;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.anfema.ionclient.IonClient;
import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.caching.FilePaths;
import com.anfema.ionclient.interceptors.AdditionalHeadersInterceptor;
import com.anfema.ionclient.interceptors.AuthorizationHeaderInterceptor;
import com.anfema.ionclient.interceptors.RequestLogger;
import com.anfema.ionclient.pages.models.contents.Downloadable;
import com.anfema.ionclient.utils.IonLog;
import com.anfema.utils.ExceptionUtils;
import com.anfema.utils.Log;
import com.anfema.utils.NetworkUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

import static com.squareup.picasso.MemoryPolicy.NO_CACHE;

/**
 * This class holds multiple {@link Picasso} instances.
 * <p>
 * Each instance intercepts the requests being performed by adding an authorization header – which is useful in case the link is referring to
 * the protected media repository of ION.
 * <p>
 * Although, the Picasso instance is accessible directly through {@link #getPicassoInstance()} or {@link #getPicassoInstanceDoAuthCall()}, loading
 * images should be performed through {@link #loadImage(String, ImageView, Function)} or one of its variations in order to utilize the persistent
 * ION disk cache.
 */
public class IonPicassoWithCaching implements IonPicasso
{
	private static final int MAX_IMAGE_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
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
		this.picasso = createPicassoInstance(
				this.config::getAuthorizationHeaderValue,
				config.additionalHeaders,
				config,
				context,
				config.networkTimeout
		);
	}

	@Override
	public void updateConfig( IonConfig config )
	{
		this.config = config;
	}

	/**
	 * You may not want to acquire a Picasso instance via {@link IonClient}.
	 */
	public static Picasso createPicassoInstance(
			Callable<String> authHeaderValueRetriever,
			@NonNull Map<String, String> additionalHeaders,
			IonConfig config,
			Context context,
			int networkTimeout
	)
	{
		OkHttpClient.Builder okHttpClientBuilder = new Builder();
		NetworkUtils.applyTimeout( okHttpClientBuilder, networkTimeout );
		okHttpClientBuilder.addInterceptor( new AuthorizationHeaderInterceptor( authHeaderValueRetriever ) );
		okHttpClientBuilder.addInterceptor( new AdditionalHeadersInterceptor( additionalHeaders ) );
		okHttpClientBuilder.addInterceptor( new RequestLogger( "Picasso Request" ) );
		// set a disk cache in OkHttp client, in case images are loaded directly through the picasso
		// instance instead of using a loadImage-method
		File mediaFolderPath = FilePaths.getMediaFolderPath( config, context, false );
		okHttpClientBuilder.cache( new Cache( mediaFolderPath, MAX_IMAGE_DISK_CACHE_SIZE ) );
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
	public static void setupDefaultPicasso(
			String authHeaderValue,
			@NonNull Map<String, String> additionalHeaders,
			IonConfig config,
			Context context,
			int networkTimeout
	) throws IllegalStateException
	{
		Picasso picasso = createPicassoInstance( () -> authHeaderValue, additionalHeaders, config, context, networkTimeout );
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

		// FIXME This subscription leaks the view
		fetchImageFile( Uri.parse( path ), checksum )
				.observeOn( AndroidSchedulers.mainThread() )
				.subscribe( fileUri -> showImage( fileUri, target, requestTransformation, callback ), throwable -> imageDownloadFailed( throwable, target, requestTransformation, callback ) );
	}

	private Single<UriWithStatus> fetchImageFile( @NonNull Uri uri, @Nullable String checksum )
	{
		if ( URLUtil.isNetworkUrl( uri.toString() ) )
		{
			HttpUrl httpUrl = HttpUrl.parse( uri.toString() );
			return ionFiles.request( httpUrl, checksum )
					.map( UriWithStatus::new );
		}
		else
		{
			return Single.just( new UriWithStatus( uri, FileStatus.DISK ) );
		}
	}

	private void showImage( @NonNull UriWithStatus uriWithStatus, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation, Callback callback )
	{
		RequestCreator requestCreator = picasso.load( uriWithStatus.uri );

		if ( uriWithStatus.status == FileStatus.NETWORK )
		{
			// skip memory cache lookup if the image has been downloaded (so memory cache may hold an outdated version)
			requestCreator.memoryPolicy( NO_CACHE );
		}

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
		showImage( new UriWithStatus( null, null ), target, requestTransformation, null );
		if ( callback != null )
		{
			callback.onError( ExceptionUtils.fromThrowable( throwable ) );
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
