package com.anfema.ampclient;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anfema.ampclient.caching.CacheUtils;
import com.anfema.ampclient.caching.CollectionCacheMeta;
import com.anfema.ampclient.caching.PageCacheMeta;
import com.anfema.ampclient.exceptions.AmpClientConfigInstantiateException;
import com.anfema.ampclient.exceptions.NetworkRequestException;
import com.anfema.ampclient.exceptions.ReadFromCacheException;
import com.anfema.ampclient.models.Collection;
import com.anfema.ampclient.models.Page;
import com.anfema.ampclient.models.PagePreview;
import com.anfema.ampclient.models.responses.CollectionResponse;
import com.anfema.ampclient.models.responses.PageResponse;
import com.anfema.ampclient.serialization.GsonFactory;
import com.anfema.ampclient.service.AmpApiFactory;
import com.anfema.ampclient.service.AmpApiRx;
import com.anfema.ampclient.service.AmpCall;
import com.anfema.ampclient.service.AmpRequestLogger;
import com.anfema.ampclient.service.CachingInterceptor;
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

	private static Map<Class<? extends AmpClientConfig>, AmpClient> instances = new HashMap<>();

	/**
	 * @param configClass implementation of AmpClientConfig interface
	 * @return client instance, ready to go (with API token set)
	 */
	public static Observable<AmpClient> getInstance( Class<? extends AmpClientConfig> configClass, Context appContext )
	{
		if ( instances == null )
		{
			instances = new HashMap<>();
		}

		// check if client for this configuration already exists, otherwise create an instance
		AmpClient client = instances.get( configClass );
		if ( client == null )
		{
			try
			{
				client = new AmpClient( configClass, appContext );
			}
			catch ( AmpClientConfigInstantiateException e )
			{
				return Observable.error( e );
			}
		}
		else if ( client.appContext == null )
		{
			// fail early if app context is null
			if ( appContext == null )
			{
				return Observable.error( new NullPointerException( "App context is null" ) );
			}
			// update appContext for existing clients if it became null
			client.appContext = appContext;
		}

		// At this point the client is initialized, but might not have an API token yet.

		return client.getInstanceWithAuthToken( appContext );
	}

	/**
	 * Asynchronously equests API token if not available yet.
	 *
	 * @return Observable emitting initialized client having an API token
	 */
	@NonNull
	private Observable<AmpClient> getInstanceWithAuthToken( Context appContext )
	{
		Observable<AmpClient> clientObservable;
		if ( authHeaderValue != null || !NetworkUtils.isConnected( appContext ) )
		{
			// authHeaderValue is available or offline mode where no request are fired and, thus, no API token is required
			clientObservable = Observable.just( this );
		}
		else
		{
			// retrieve API token
			final AmpClient finalClient = this;
			clientObservable = ampClientConfig.retrieveApiToken( appContext )
					.doOnNext( this::updateApiToken )
					.doOnNext( apiToken -> Log.i( "received API token: " + apiToken ) )
					.map( apiToken -> finalClient );
		}
		return clientObservable
				.doOnError( RxUtils.DEFAULT_EXCEPTION_HANDLER );
	}

	/// Multiton END


	/// configuration

	private Context         appContext;
	private AmpClientConfig ampClientConfig;
	private AmpApiRx        ampApi;
	private String          authHeaderValue;

	private AmpClient( Class<? extends AmpClientConfig> configClass, Context appContext ) throws AmpClientConfigInstantiateException
	{
		this.appContext = appContext;

		//noinspection TryWithIdenticalCatches
		try
		{
			ampClientConfig = configClass.newInstance();
			List<Interceptor> interceptors = new ArrayList<>();
			interceptors.add( new AmpRequestLogger( "Network Request" ) );
			interceptors.add( new CachingInterceptor( appContext ) );
			ampApi = AmpApiFactory.newInstance( ampClientConfig.getBaseUrl( appContext ), interceptors, AmpApiRx.class );

			instances.put( configClass, this );
		}
		catch ( InstantiationException e )
		{
			throw new AmpClientConfigInstantiateException();
		}
		catch ( IllegalAccessException e )
		{
			throw new AmpClientConfigInstantiateException();
		}
	}

	private void updateApiToken( String apiToken )
	{
		authHeaderValue = "token " + apiToken;
	}

	/// configuration END

	/// API Interface

	/**
	 * Add collection identifier and authorization token to request.
	 */
	@Override
	public Observable<Collection> getCollection( String collectionIdentifier )
	{
		String collectionUrl = getCollectionUrl( collectionIdentifier );
		CollectionCacheMeta cacheMeta = CollectionCacheMeta.retrieve( collectionUrl, appContext );

		if ( !NetworkUtils.isConnected( appContext ) )
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
	 * Use default collection identifier as specified in {@link AmpClientConfig#getCollectionIdentifier(Context)}
	 */
	@Override
	public Observable<Collection> getCollection()
	{
		return getCollection( ampClientConfig.getCollectionIdentifier( appContext ) );
	}

	/**
	 * Add collection identifier and authorization token to request
	 */
	@Override
	public Observable<Page> getPage( String collectionIdentifier, String pageIdentifier )
	{
		String pageUrl = getPageUrl( collectionIdentifier, pageIdentifier );
		PageCacheMeta pageCacheMeta = PageCacheMeta.retrieve( pageUrl, appContext );

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
					if ( !NetworkUtils.isConnected( appContext ) )
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
	 * Use default collection identifier as specified in {@link AmpClientConfig#getCollectionIdentifier(Context)}
	 */
	@Override
	public Observable<Page> getPage( String pageIdentifier )
	{
		return getPage( ampClientConfig.getCollectionIdentifier( appContext ), pageIdentifier );
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
	 * Use default collection identifier as specified in {@link AmpClientConfig#getCollectionIdentifier(Context)}
	 */
	@Override
	public Observable<Page> getAllPages()
	{
		return getAllPages( ampClientConfig.getCollectionIdentifier( appContext ) );
	}

	public Observable<Page> getSomePages( String collectionIdentifier, Func1<PagePreview, Boolean> pagesFilter )
	{
		return getCollection( collectionIdentifier )
				.map( collection -> collection.pages )
				.flatMap( Observable::from )
				.filter( pagesFilter )
				.map( page -> page.identifier )
				.flatMap( this::getPage );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.<br/>
	 * Use default collection identifier as specified in {@link AmpClientConfig#getCollectionIdentifier(Context)}
	 */
	public Observable<Page> getSomePages( Func1<PagePreview, Boolean> pagesFilter )
	{
		return getSomePages( ampClientConfig.getCollectionIdentifier( appContext ), pagesFilter );
	}


	/// API Interface END


	/// Get collection methods

	private Observable<Collection> getCollectionFromCache( String collectionIdentifier, boolean serverCallAsBackup )
	{
		String collectionUrl = getCollectionUrl( collectionIdentifier );
		Log.i( "Cache Request", collectionUrl );
		try
		{
			String filePath = CacheUtils.getFilePath( collectionUrl, appContext );
			return FileUtils
					.readFromFile( filePath )
					.map( collectionsString -> GsonFactory.newInstance().fromJson( collectionsString, CollectionResponse.class ) )
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
		return ampApi.getCollection( collectionIdentifier, authHeaderValue )
				.map( CollectionResponse::getCollection )
				.doOnNext( saveCollectionMeta() )
				.compose( RxUtils.applySchedulers() )
				.onErrorResumeNext( throwable -> {
					String collectionUrl = getCollectionUrl( collectionIdentifier );
					if ( cacheAsBackup )
					{
						Log.w( "Backup Request", "Network request " + collectionUrl + " failed. Trying cache request instead..." );
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
			String filePath = CacheUtils.getFilePath( pageUrl, appContext );
			return FileUtils
					.readFromFile( filePath )
					.map( pagesString -> GsonFactory.newInstance().fromJson( pagesString, PageResponse.class ) )
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
		return ampApi.getPage( collectionIdentifier, pageIdentifier, authHeaderValue )
				.map( PageResponse::getPage )
				.doOnNext( savePageMeta() )
				.compose( RxUtils.applySchedulers() )
				.onErrorResumeNext( throwable -> {
					String pageUrl = getPageUrl( collectionIdentifier, pageIdentifier );
					if ( cacheAsBackup )
					{
						Log.w( "Backup Request", "Network request " + pageUrl + " failed. Trying cache request instead..." );
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
			CollectionCacheMeta.save( url, cacheMeta, appContext );
		};
	}

	@NonNull
	private Action1<Page> savePageMeta()
	{
		return page -> {
			String url = getPageUrl( page.collection, page.identifier );
			PageCacheMeta cacheMeta = new PageCacheMeta( url, page.last_changed );
			PageCacheMeta.save( url, cacheMeta, appContext );
		};
	}

	private String getCollectionUrl( String collectionId )
	{
		String baseUrl = ampClientConfig.getBaseUrl( appContext );
		return baseUrl + AmpCall.COLLECTIONS.toString() + FileUtils.SLASH + collectionId;
	}

	private String getPageUrl( String collectionId, String pageId )
	{
		String baseUrl = ampClientConfig.getBaseUrl( appContext );
		return baseUrl + AmpCall.PAGES.toString() + FileUtils.SLASH + collectionId + FileUtils.SLASH + pageId;
	}
}
