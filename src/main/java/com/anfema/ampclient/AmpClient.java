package com.anfema.ampclient;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anfema.ampclient.caching.CacheUtils;
import com.anfema.ampclient.caching.CollectionCacheMeta;
import com.anfema.ampclient.caching.PageCacheMeta;
import com.anfema.ampclient.exceptions.AmpConfigInvalidException;
import com.anfema.ampclient.exceptions.ContextIsNullException;
import com.anfema.ampclient.exceptions.NetworkRequestException;
import com.anfema.ampclient.exceptions.ReadFromCacheException;
import com.anfema.ampclient.fulltextsearch.AmpFts;
import com.anfema.ampclient.interceptors.CachingInterceptor;
import com.anfema.ampclient.interceptors.DeviceIdHeaderInterceptor;
import com.anfema.ampclient.interceptors.RequestLogger;
import com.anfema.ampclient.models.Collection;
import com.anfema.ampclient.models.Page;
import com.anfema.ampclient.models.PagePreview;
import com.anfema.ampclient.models.responses.CollectionResponse;
import com.anfema.ampclient.models.responses.PageResponse;
import com.anfema.ampclient.serialization.GsonHolder;
import com.anfema.ampclient.service.AmpApiFactory;
import com.anfema.ampclient.service.AmpApiRx;
import com.anfema.ampclient.service.AmpCall;
import com.anfema.ampclient.utils.ContextUtils;
import com.anfema.ampclient.utils.DateTimeUtils;
import com.anfema.ampclient.utils.FileUtils;
import com.anfema.ampclient.utils.Log;
import com.anfema.ampclient.utils.NetworkUtils;
import com.anfema.ampclient.utils.RxUtils;
import com.squareup.okhttp.Interceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class AmpClient implements AmpClientApi
{
	/// Multiton

	private static Map<AmpConfig, AmpClient> instances = new HashMap<>();

	/**
	 * @param config configuration for AMP client
	 * @return client instance, ready to go (with API token set)
	 */
	public static AmpClient getInstance( AmpConfig config, Context context )
	{
		context = ContextUtils.getApplicationContext( context );

		if ( instances == null )
		{
			instances = new HashMap<>();
		}

		if ( !config.isValid() )
		{
			throw new AmpConfigInvalidException();
		}

		// check if client for this configuration already exists, otherwise create an instance
		AmpClient storedClient = instances.get( config );
		if ( storedClient != null )
		{
			if ( storedClient.context == null )
			{
				// fail early if app context is null
				if ( context == null )
				{
					throw new ContextIsNullException();
				}
				// update context for existing clients if it became null
				storedClient.context = context;
			}
			return storedClient;
		}

		AmpClient ampClient = new AmpClient( config, context );
		instances.put( config, ampClient );
		return ampClient;
	}

	/// Multiton END


	/// configuration

	private Context   context;
	private AmpConfig config;
	private AmpApiRx  ampApi;

	private AmpClient( AmpConfig config, Context context )
	{
		this.context = context;
		this.config = config;

		List<Interceptor> interceptors = new ArrayList<>();
		interceptors.add( new DeviceIdHeaderInterceptor( context ) );
		interceptors.add( new RequestLogger( "Network Request" ) );
		interceptors.add( new CachingInterceptor( context ) );
		ampApi = AmpApiFactory.newInstance( config.baseUrl, interceptors, AmpApiRx.class );

		ampFts = new AmpFts( this, this.config, this.context );

		instances.put( config, this );
	}

	/// configuration END

	/// API Interface

	/**
	 * Add collection identifier and authorization token to request.<br/>
	 * Use default collection identifier as specified in {@link this#config}
	 */
	@Override
	public Observable<Collection> getCollection()
	{
		String collectionUrl = getCollectionUrl();
		CollectionCacheMeta cacheMeta = CollectionCacheMeta.retrieve( collectionUrl, context );

		if ( !NetworkUtils.isConnected( context ) )
		{
			return getCollectionFromCache( false );
		}
		else if ( cacheMeta == null || cacheMeta.isOutdated() )
		{
			return getCollectionFromServer( false );
		}
		else
		{
			return getCollectionFromCache( true );
		}
	}

	/**
	 * Add collection identifier and authorization token to request.<br/>
	 * Use default collection identifier as specified in {@link this#config}
	 */
	@Override
	public Observable<Page> getPage( String pageIdentifier )
	{
		String pageUrl = getPageUrl( pageIdentifier );
		PageCacheMeta pageCacheMeta = PageCacheMeta.retrieve( pageUrl, context );

		if ( pageCacheMeta == null )
		{
			return getPageFromServer( pageIdentifier, false );
		}

		return getCollection()
				// get page's last_changed date from collection
				.flatMap( collection -> collection.getPageLastChanged( pageIdentifier ) )
						// compare last_changed date of cached page with that of collection
				.map( pageCacheMeta::isOutdated )
				.flatMap( isOutdated -> {
					if ( !NetworkUtils.isConnected( context ) )
					{
						return getPageFromCache( pageIdentifier, false );
					}
					else if ( isOutdated )
					{
						return getPageFromServer( pageIdentifier, false );
					}
					else
					{
						return getPageFromCache( pageIdentifier, true );
					}
				} );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.<br/>
	 * Use collection identifier as specified in {@link this#config}
	 */
	@Override
	public Observable<Page> getAllPages()
	{
		return getCollection()
				.map( collection -> collection.pages )
				.flatMap( Observable::from )
				.map( page -> page.identifier )
				.flatMap( this::getPage );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.<br/>
	 * Use collection identifier as specified in {@link this#config}
	 */
	public Observable<Page> getPages( Func1<PagePreview, Boolean> pagesFilter )
	{
		return getCollection()
				.map( collection -> collection.pages )
				.flatMap( Observable::from )
				.filter( pagesFilter )
				.map( page -> page.identifier )
				.flatMap( this::getPage );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.<br/>
	 * Use default collection identifier as specified in {@link this#config}
	 */
	public Observable<Page> getPagesOrdered( Func1<PagePreview, Boolean> pagesFilter )
	{
		return getCollection()
				.map( collection -> collection.pages )
				.concatMap( Observable::from )
				.filter( pagesFilter )
				.map( page -> page.identifier )
				.concatMap( this::getPage );
	}


	/// API Interface END


	/// Get collection methods

	private Observable<Collection> getCollectionFromCache( boolean serverCallAsBackup )
	{
		String collectionUrl = getCollectionUrl();
		Log.i( "Cache Request", collectionUrl );
		try
		{
			String filePath = CacheUtils.getFilePath( collectionUrl, context );
			return FileUtils
					.readFromFile( filePath )
					.map( collectionsString -> GsonHolder.getInstance().fromJson( collectionsString, CollectionResponse.class ) )
					.map( CollectionResponse::getCollection )
					.compose( RxUtils.runOnIoThread() )
					.onErrorResumeNext( throwable -> {
						return handleUnsuccessfulCollectionCacheReading( collectionUrl, serverCallAsBackup, throwable );
					} );
		}
		catch ( IOException e )
		{
			return handleUnsuccessfulCollectionCacheReading( collectionUrl, serverCallAsBackup, e );
		}
	}

	private Observable<Collection> handleUnsuccessfulCollectionCacheReading( String collectionUrl, boolean serverCallAsBackup, Throwable e )
	{
		if ( serverCallAsBackup )
		{
			Log.w( "Backup Request", "Cache request " + collectionUrl + " failed. Trying network request instead..." );
			Log.ex( "Cache Request", e );
			return getCollectionFromServer( false );
		}
		else
		{
			Log.e( "Failed Request", "Cache request " + collectionUrl + " failed." );
			return Observable.error( new ReadFromCacheException( collectionUrl, e ) );
		}
	}

	private Observable<Collection> getCollectionFromServer( boolean cacheAsBackup )
	{
		return ampApi.getCollection( config.collectionIdentifier, config.authorizationHeaderValue )
				.map( CollectionResponse::getCollection )
				.doOnNext( saveCollectionMeta() )
				.compose( RxUtils.runOnIoThread() )
				.onErrorResumeNext( throwable -> {
					String collectionUrl = getCollectionUrl();
					if ( cacheAsBackup )
					{
						Log.w( "Backup Request", "Network request " + collectionUrl + " failed. Trying cache request instead..." );
						Log.ex( "Network Request", throwable );
						return getCollectionFromCache( false );
					}
					Log.e( "Failed Request", "Network request " + collectionUrl + " failed." );
					return Observable.error( new NetworkRequestException( collectionUrl, throwable ) );
				} );
	}


	/// Get collection methods END


	/// Get page methods

	/**
	 * @param serverCallAsBackup If reading from cache is not successful, should a server call be made?
	 */
	private Observable<Page> getPageFromCache( String pageIdentifier, boolean serverCallAsBackup )
	{
		String pageUrl = getPageUrl( pageIdentifier );
		Log.i( "Cache Request", pageUrl );
		try
		{
			String filePath = CacheUtils.getFilePath( pageUrl, context );
			return FileUtils
					.readFromFile( filePath )
					.map( pagesString -> GsonHolder.getInstance().fromJson( pagesString, PageResponse.class ) )
					.map( PageResponse::getPage )
					.compose( RxUtils.runOnIoThread() )
					.onErrorResumeNext( throwable -> {
						return handleUnsuccessfulPageCacheReading( pageIdentifier, serverCallAsBackup, pageUrl, throwable );
					} );
		}
		catch ( IOException e )
		{
			return handleUnsuccessfulPageCacheReading( pageIdentifier, serverCallAsBackup, pageUrl, e );
		}
	}

	private Observable<Page> handleUnsuccessfulPageCacheReading( String pageIdentifier, boolean serverCallAsBackup, String pageUrl, Throwable e )
	{
		if ( serverCallAsBackup )
		{
			Log.w( "Backup Request", "Cache request " + pageUrl + " failed. Trying network request instead..." );
			Log.ex( "Cache Request", e );
			return getPageFromServer( pageIdentifier, false );
		}
		Log.e( "Failed Request", "Cache request " + pageUrl + " failed." );
		return Observable.error( new ReadFromCacheException( pageUrl, e ) );
	}

	/**
	 * @param cacheAsBackup If server call is not successful, should cached version be used (even if it might be old)?
	 */
	private Observable<Page> getPageFromServer( String pageIdentifier, boolean cacheAsBackup )
	{
		return ampApi.getPage( config.collectionIdentifier, pageIdentifier, config.authorizationHeaderValue )
				.map( PageResponse::getPage )
				.doOnNext( savePageMeta() )
				.compose( RxUtils.runOnIoThread() )
				.onErrorResumeNext( throwable -> {
					String pageUrl = getPageUrl( pageIdentifier );
					if ( cacheAsBackup )
					{
						Log.w( "Backup Request", "Network request " + pageUrl + " failed. Trying cache request instead..." );
						Log.ex( "Network Request", throwable );
						return getPageFromCache( pageIdentifier, false );
					}
					Log.e( "Failed Request", "Network request " + pageUrl + " failed." );
					return Observable.error( new NetworkRequestException( pageUrl, throwable ) );
				} );
	}

	/// Get page methods END


	@NonNull
	private Action1<Collection> saveCollectionMeta()
	{
		return collection -> {
			String url = getCollectionUrl();
			CollectionCacheMeta cacheMeta = new CollectionCacheMeta( url, DateTimeUtils.now() );
			CollectionCacheMeta.save( url, cacheMeta, context );
		};
	}

	@NonNull
	private Action1<Page> savePageMeta()
	{
		return page -> {
			String url = getPageUrl( page.identifier );
			PageCacheMeta cacheMeta = new PageCacheMeta( url, page.last_changed );
			PageCacheMeta.save( url, cacheMeta, context );
		};
	}

	private String getCollectionUrl()
	{
		String baseUrl = config.baseUrl;
		return baseUrl + AmpCall.COLLECTIONS.toString() + FileUtils.SLASH + config.collectionIdentifier;
	}

	private String getPageUrl( String pageId )
	{
		String baseUrl = config.baseUrl;
		return baseUrl + AmpCall.PAGES.toString() + FileUtils.SLASH + config.collectionIdentifier + FileUtils.SLASH + pageId;
	}


	/// Full text search

	private final AmpFts ampFts;

	public AmpFts fts()
	{
		return ampFts;
	}

	/// Full text search END
}
