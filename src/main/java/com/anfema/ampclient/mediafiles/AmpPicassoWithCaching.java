package com.anfema.ampclient.mediafiles;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.interceptors.AuthorizationHeaderInterceptor;
import com.anfema.ampclient.interceptors.RequestLogger;
import com.anfema.ampclient.utils.Log;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import rx.Observable;
import rx.functions.Func1;

/**
 * This class holds multiple {@link Picasso} instances.
 * <p>
 * Each instance intercepts the requests being performed by adding an authorization header – which is useful in case the link is referring to
 * the protected media repository of AMP.
 */
public class AmpPicassoWithCaching implements AmpPicasso
{
	private final AmpFiles ampFiles;
	private final Picasso  picasso;

	public AmpPicassoWithCaching( AmpFiles ampFiles, AmpConfig config, Context context )
	{
		this.ampFiles = ampFiles;
		this.picasso = createPicassoInstance( config.authorizationHeaderValue, context );
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
	 * <p>
	 * Caution: if Picasso.with(this) has been called before an instance is already set and this method will throw an exception.
	 * <p>
	 * Therefore, it is recommended to call this method in {@link Application#onCreate()}. (But do not perform any long-lasting operations there.)
	 */
	public static void setupDefaultPicasso( String authHeaderValue, Context context ) throws IllegalStateException
	{
		Picasso picasso = createPicassoInstance( authHeaderValue, context );
		Picasso.setSingletonInstance( picasso );
	}

	@Override
	public void loadImage( String path, ImageView target, Func1<RequestCreator, RequestCreator> requestTransformation )
	{
		if ( path == null || path.trim().length() == 0 )
		{
			// let picasso handle edge cases
			picasso.load( path );
			return;
		}
		loadImage( Uri.parse( path ), target, requestTransformation );
	}

	@Override
	public void loadImage( Uri requestUri, ImageView target, Func1<RequestCreator, RequestCreator> requestTransformation )
	{
		Log.i( "AMP Picasso", "START: requestUri: " + requestUri );
		fetchImageFile( requestUri )
				.subscribe( fileUri -> showImage( fileUri, target, requestTransformation ), throwable -> {
					Log.e( "AMP Picasso", "requestUri: " + requestUri + ", target: " + target );
					Log.ex( "AMP Picasso", throwable );
				} );
	}

	private Observable<Uri> fetchImageFile( Uri uri )
	{
		if ( URLUtil.isNetworkUrl( uri.toString() ) )
		{
			HttpUrl httpUrl = HttpUrl.parse( uri.toString() );
			return ampFiles.request( httpUrl, null )
					.map( Uri::fromFile );
		}
		else
		{
			return Observable.just( uri );
		}
	}

	private void showImage( Uri uri, ImageView target, Func1<RequestCreator, RequestCreator> requestTransformation )
	{
		RequestCreator requestCreator = picasso.load( uri );
		// apply requestCreator operations
		requestCreator = requestTransformation.call( requestCreator );

		requestCreator.into( target );
	}

	@Override
	public Picasso getPicassoInstance()
	{
		return picasso;
	}
}
