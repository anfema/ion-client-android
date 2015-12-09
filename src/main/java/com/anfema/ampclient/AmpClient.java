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
		if ( storedClient == null )
		{
			AmpClient ampClient = new AmpClient( config, context );
			instances.put( config, ampClient );
			return ampClient;
		}

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

		instances.put( config, this );
	}

	/// configuration END

	/// API Interface

	/**
	 * Add collection identifier and authorization header to request.
	 */
	@Override
	public Observable<Collection> getCollection( String collectionIdentifier )
	{
		String collectionUrl = getCollectionUrl( collectionIdentifier );
		CollectionCacheMeta cacheMeta = CollectionCacheMeta.retrieve( collectionUrl, context );

		if ( !NetworkUtils.isConnected( context ) )
		{
			return getCollectionFromCache( collectionIdentifier, false );
		}
		else if ( cacheMeta == null || cacheMeta.isOutdated() )
		{
			return getCollectionFromServer( collectionIdentifier, true );
		}
		else
		{
			return getCollectionFromCache( collectionIdentifier, true );
		}
	}

	/**
	 * Add collection identifier and authorization token to request.<br/>
	 * Use default collection identifier as specified in {@link this#config}
	 */
	@Override
	public Observable<Collection> getCollection()
	{
		return getCollection( config.defaultCollectionIdentifier );
	}

	/**
	 * Add collection identifier and authorization token to request
	 */
	@Override
	public Observable<Page> getPage( String collectionIdentifier, String pageIdentifier )
	{
		String pageUrl = getPageUrl( collectionIdentifier, pageIdentifier );
		PageCacheMeta pageCacheMeta = PageCacheMeta.retrieve( pageUrl, context );

		if ( pageCacheMeta == null )
		{
			return getPageFromServer( collectionIdentifier, pageIdentifier, false );
		}

		return getCollection( collectionIdentifier )
				// get page's last_changed date from collection
				.flatMap( collection -> collection.getPageLastChanged( pageIdentifier ) )
						// compare last_changed date of cached page with that of collection
				.map( pageCacheMeta::isOutdated )
				.flatMap( isOutdated -> {
					if ( !NetworkUtils.isConnected( context ) )
					{
						return getPageFromCache( collectionIdentifier, pageIdentifier, false );
					}
					else if ( isOutdated )
					{
						return getPageFromServer( collectionIdentifier, pageIdentifier, true );
					}
					else
					{
						return getPageFromCache( collectionIdentifier, pageIdentifier, true );
					}
				} );
	}

	/**
	 * Add collection identifier and authorization token to request.<br/>
	 * Use default collection identifier as specified in {@link this#config}
	 */
	@Override
	public Observable<Page> getPage( String pageIdentifier )
	{
		return getPage( config.defaultCollectionIdentifier, pageIdentifier );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events
	 */
	@Override
	public Observable<Page> getAllPages( String collectionIdentifier )
	{
		return getCollection( collectionIdentifier )
				.map( collection -> collection.pages )
				.flatMap( Observable::from )
				.map( page -> page.identifier )
				.flatMap( this::getPage );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.<br/>
	 * Use default collection identifier as specified in {@link this#config}
	 */
	@Override
	public Observable<Page> getAllPages()
	{
		return getAllPages( config.defaultCollectionIdentifier );
	}

	public Observable<Page> getPages( String collectionIdentifier, Func1<PagePreview, Boolean> pagesFilter )
	{
		return getCollection( collectionIdentifier )
				.map( collection -> collection.pages )
				.flatMap( Observable::from )
				.filter( pagesFilter )
				.map( page -> page.identifier )
				.flatMap( pageIdentifier -> getPage( collectionIdentifier, pageIdentifier ) );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.<br/>
	 * Use default collection identifier as specified in {@link this#config}
	 */
	public Observable<Page> getPages( Func1<PagePreview, Boolean> pagesFilter )
	{
		return getPages( config.defaultCollectionIdentifier, pagesFilter );
	}

	public Observable<Page> getPagesOrdered( String collectionIdentifier, Func1<PagePreview, Boolean> pagesFilter )
	{
		return getCollection( collectionIdentifier )
				.map( collection -> collection.pages )
				.concatMap( Observable::from )
				.filter( pagesFilter )
				.map( page -> page.identifier )
				.concatMap( pageIdentifier -> getPage( collectionIdentifier, pageIdentifier ) );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.<br/>
	 * Use default collection identifier as specified in {@link this#config}
	 */
	public Observable<Page> getPagesOrdered( Func1<PagePreview, Boolean> pagesFilter )
	{
		return getPagesOrdered( config.defaultCollectionIdentifier, pagesFilter );
	}


	/// API Interface END


	/// Get collection methods

	private Observable<Collection> getCollectionFromCache( String collectionIdentifier, boolean serverCallAsBackup )
	{
		String collectionUrl = getCollectionUrl( collectionIdentifier );
		Log.i( "Cache Request", collectionUrl );
		try
		{
			String filePath = CacheUtils.getFilePath( collectionUrl, context );
			return FileUtils
					.readFromFile( filePath )
					.map( collectionsString -> GsonHolder.getInstance().fromJson( collectionsString, CollectionResponse.class ) )
					.map( CollectionResponse::getCollection )
					.compose( RxUtils.applySchedulers() )
					.onErrorResumeNext( throwable -> {
						return handleUnsuccessfulCollectionCacheReading( collectionIdentifier, collectionUrl, serverCallAsBackup, throwable );
					} );
		}
		catch ( IOException e )
		{
			return handleUnsuccessfulCollectionCacheReading( collectionIdentifier, collectionUrl, serverCallAsBackup, e );
		}
	}

	private Observable<Collection> handleUnsuccessfulCollectionCacheReading( String collectionIdentifier, String collectionUrl, boolean serverCallAsBackup, Throwable e )
	{
		if ( serverCallAsBackup )
		{
			Log.w( "Backup Request", "Cache request " + collectionUrl + " failed. Trying network request instead..." );
			Log.ex( "Cache Request", e );
			return getCollectionFromServer( collectionIdentifier, false );
		}
		else
		{
			Log.e( "Failed Request", "Cache request " + collectionUrl + " failed." );
			return Observable.error( new ReadFromCacheException( collectionUrl, e ) );
		}
	}

	private Observable<Collection> getCollectionFromServer( String collectionIdentifier, boolean cacheAsBackup )
	{
		return ampApi.getCollection( collectionIdentifier, config.authorizationHeaderValue )
				.map( CollectionResponse::getCollection )
				.doOnNext( saveCollectionMeta() )
				.compose( RxUtils.applySchedulers() )
				.onErrorResumeNext( throwable -> {
					String collectionUrl = getCollectionUrl( collectionIdentifier );
					if ( cacheAsBackup )
					{
						Log.w( "Backup Request", "Network request " + collectionUrl + " failed. Trying cache request instead..." );
						Log.ex( "Network Request", throwable );
						return getCollectionFromCache( collectionIdentifier, false );
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
	private Observable<Page> getPageFromCache( String collectionIdentifier, String pageIdentifier, boolean serverCallAsBackup )
	{
		String pageUrl = getPageUrl( collectionIdentifier, pageIdentifier );
		Log.i( "Cache Request", pageUrl );
		try
		{
			String filePath = CacheUtils.getFilePath( pageUrl, context );
			return FileUtils
					.readFromFile( filePath )
					.map( pagesString -> GsonHolder.getInstance().fromJson( pagesString, PageResponse.class ) )
					.map( PageResponse::getPage )
					.compose( RxUtils.applySchedulers() )
					.onErrorResumeNext( throwable -> {
						return handleUnsuccessfulPageCacheReading( collectionIdentifier, pageIdentifier, serverCallAsBackup, pageUrl, throwable );
					} );
		}
		catch ( IOException e )
		{
			return handleUnsuccessfulPageCacheReading( collectionIdentifier, pageIdentifier, serverCallAsBackup, pageUrl, e );
		}
	}

	private Observable<Page> handleUnsuccessfulPageCacheReading( String collectionIdentifier, String pageIdentifier, boolean serverCallAsBackup, String pageUrl, Throwable e )
	{
		if ( serverCallAsBackup )
		{
			Log.w( "Backup Request", "Cache request " + pageUrl + " failed. Trying network request instead..." );
			Log.ex( "Cache Request", e );
			return getPageFromServer( collectionIdentifier, pageIdentifier, false );
		}
		Log.e( "Failed Request", "Cache request " + pageUrl + " failed." );
		return Observable.error( new ReadFromCacheException( pageUrl, e ) );
	}

	/**
	 * @param cacheAsBackup If server call is not successful, should cached version be used (even if it might be old)?
	 */
	private Observable<Page> getPageFromServer( String collectionIdentifier, String pageIdentifier, boolean cacheAsBackup )
	{
		return ampApi.getPage( collectionIdentifier, pageIdentifier, config.authorizationHeaderValue )
				.map( PageResponse::getPage )
				.doOnNext( savePageMeta() )
				.compose( RxUtils.applySchedulers() )
				.onErrorResumeNext( throwable -> {
					String pageUrl = getPageUrl( collectionIdentifier, pageIdentifier );
					if ( cacheAsBackup )
					{
						Log.w( "Backup Request", "Network request " + pageUrl + " failed. Trying cache request instead..." );
						Log.ex( "Network Request", throwable );
						return getPageFromCache( collectionIdentifier, pageIdentifier, false );
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
			String url = getCollectionUrl( collection.identifier );
			CollectionCacheMeta cacheMeta = new CollectionCacheMeta( url, DateTimeUtils.now() );
			CollectionCacheMeta.save( url, cacheMeta, context );
		};
	}

	@NonNull
	private Action1<Page> savePageMeta()
	{
		return page -> {
			String url = getPageUrl( page.collection, page.identifier );
			PageCacheMeta cacheMeta = new PageCacheMeta( url, page.last_changed );
			PageCacheMeta.save( url, cacheMeta, context );
		};
	}

	private String getCollectionUrl( String collectionId )
	{
		String baseUrl = config.baseUrl;
		return baseUrl + AmpCall.COLLECTIONS.toString() + FileUtils.SLASH + collectionId;
	}

	private String getPageUrl( String collectionId, String pageId )
	{
		String baseUrl = config.baseUrl;
		return baseUrl + AmpCall.PAGES.toString() + FileUtils.SLASH + collectionId + FileUtils.SLASH + pageId;
	}
}
