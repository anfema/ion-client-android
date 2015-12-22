package com.anfema.ampclient.pages;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.caching.CollectionCacheMeta;
import com.anfema.ampclient.caching.FilePaths;
import com.anfema.ampclient.caching.PageCacheMeta;
import com.anfema.ampclient.exceptions.NetworkRequestException;
import com.anfema.ampclient.exceptions.ReadFromCacheException;
import com.anfema.ampclient.models.Collection;
import com.anfema.ampclient.models.Page;
import com.anfema.ampclient.models.PagePreview;
import com.anfema.ampclient.models.responses.CollectionResponse;
import com.anfema.ampclient.models.responses.PageResponse;
import com.anfema.ampclient.serialization.GsonHolder;
import com.anfema.ampclient.utils.ApiFactory;
import com.anfema.ampclient.utils.DateTimeUtils;
import com.anfema.ampclient.utils.FileUtils;
import com.anfema.ampclient.utils.Log;
import com.anfema.ampclient.utils.NetworkUtils;
import com.anfema.ampclient.utils.RxUtils;
import com.squareup.okhttp.Interceptor;

import java.io.File;
import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * A wrapper of "collections" and "pages" call of AMP API.
 * <p>
 * Adds collection identifier and authorization token to request as retrieved via {@link AmpConfig}<br/>
 * <p>
 * Uses a file and a memory cache.
 */
public class AmpPagesWithCaching implements AmpPages
{
	private final Context context;

	/**
	 * Contains essential data for making calls to AMP API
	 */
	private final AmpConfig config;

	/**
	 * Access to the AMP API
	 */
	private AmpPagesApi ampApi;

	public AmpPagesWithCaching( AmpConfig config, Context context, List<Interceptor> interceptors )
	{
		this.config = config;
		this.context = context;
		ampApi = ApiFactory.newInstance( config.baseUrl, interceptors, AmpPagesApi.class );
	}

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
		return getPages( pagePreview -> true );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.<br/>
	 * Use collection identifier as specified in {@link this#config}
	 */
	@Override
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
	@Override
	public Observable<Page> getPagesOrdered( Func1<PagePreview, Boolean> pagesFilter )
	{
		return getCollection()
				.map( collection -> collection.pages )
				.concatMap( Observable::from )
				.filter( pagesFilter )
				.map( page -> page.identifier )
				.concatMap( this::getPage );
	}


	/// Get collection methods

	private Observable<Collection> getCollectionFromCache( boolean serverCallAsBackup )
	{
		String collectionUrl = getCollectionUrl();
		Log.i( "Cache Request", collectionUrl );
		try
		{
			File filePath = FilePaths.getJsonFilePath( collectionUrl, context );
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
			File filePath = FilePaths.getJsonFilePath( pageUrl, context );
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
		return baseUrl + AmpCallType.COLLECTIONS.toString() + FileUtils.SLASH + config.collectionIdentifier;
	}

	private String getPageUrl( String pageId )
	{
		String baseUrl = config.baseUrl;
		return baseUrl + AmpCallType.PAGES.toString() + FileUtils.SLASH + config.collectionIdentifier + FileUtils.SLASH + pageId;
	}
}