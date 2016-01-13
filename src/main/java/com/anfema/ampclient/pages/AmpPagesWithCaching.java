package com.anfema.ampclient.pages;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.caching.CollectionCacheIndex;
import com.anfema.ampclient.caching.FilePaths;
import com.anfema.ampclient.caching.PageCacheIndex;
import com.anfema.ampclient.exceptions.CollectionNotAvailableException;
import com.anfema.ampclient.exceptions.NetworkRequestException;
import com.anfema.ampclient.exceptions.NoAmpPagesRequestException;
import com.anfema.ampclient.exceptions.PageNotAvailableException;
import com.anfema.ampclient.exceptions.ReadFromCacheException;
import com.anfema.ampclient.pages.models.Collection;
import com.anfema.ampclient.pages.models.Page;
import com.anfema.ampclient.pages.models.PagePreview;
import com.anfema.ampclient.pages.models.responses.CollectionResponse;
import com.anfema.ampclient.pages.models.responses.PageResponse;
import com.anfema.ampclient.serialization.GsonHolder;
import com.anfema.ampclient.utils.ApiFactory;
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
 * <p/>
 * Adds collection identifier and authorization token to request as retrieved via {@link AmpConfig}<br/>
 * <p/>
 * Uses a file and a memory cache.
 */
public class AmpPagesWithCaching implements AmpPages
{

	private CollectionDownloadedListener collectionListener;

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
	 * Retrieve collection with following priorities:
	 * <p/>
	 * 1. Look if there is a current version in cache
	 * 2. Download from server if internet connection available
	 * 3. If no internet connection: return cached version (even if outdated)
	 * 4. Collection is not retrievable at all: emit error
	 * <p/>
	 * Use default collection identifier as specified in {@link this#config}
	 */
	@Override
	public Observable<Collection> getCollection()
	{
		String collectionUrl = PagesUrls.getCollectionUrl( config );
		CollectionCacheIndex cacheIndex = CollectionCacheIndex.retrieve( collectionUrl, config.collectionIdentifier, context );

		boolean currentCacheEntry = cacheIndex != null && !cacheIndex.isOutdated( config );
		boolean networkConnected = NetworkUtils.isConnected( context );

		if ( currentCacheEntry )
		{
			// retrieve current version from cache
			return getCollectionFromCache( networkConnected );
		}
		else if ( networkConnected )
		{
			// download collection
			return getCollectionFromServer( false )
					.doOnNext( collection -> {
						if ( collectionListener != null )
						{
							collectionListener.collectionDownloaded( collection, cacheIndex );
						}
					} );
		}
		else if ( cacheIndex != null )
		{
			// no network: use old version from cache
			return getCollectionFromCache( false );
		}
		else
		{
			// collection can neither be downloaded nor be found in cache
			return Observable.error( new CollectionNotAvailableException() );
		}
	}

	/**
	 * Retrieve page with following priorities:
	 * <p/>
	 * <p/>
	 * 1. Look if there is a current version in cache
	 * 2. Download from server if internet connection available
	 * 3. If no internet connection: return cached version (even if outdated)
	 * 4. Collection is not retrievable at all: emit error
	 * <p/>
	 * Add collection identifier and authorization token to request.<br/>
	 * Use default collection identifier as specified in {@link this#config}
	 */
	@Override
	public Observable<Page> getPage( String pageIdentifier )
	{
		String pageUrl = PagesUrls.getPageUrl( config, pageIdentifier );
		PageCacheIndex pageCacheIndex = PageCacheIndex.retrieve( pageUrl, config.collectionIdentifier, context );

		if ( pageCacheIndex == null )
		{
			// no cached version available: There is no need to fetch collection for date comparison.
			if ( NetworkUtils.isConnected( context ) )
			{
				// download page
				return getPageFromServer( pageIdentifier, false );
			}
			else
			{
				// page can neither be downloaded nor be found in cache
				return Observable.error( new PageNotAvailableException() );
			}
		}

		return getCollection()
				// get page's last_changed date from collection
				.flatMap( collection -> collection.getPageLastChangedAsync( pageIdentifier ) )
						// compare last_changed date of cached page with that of collection
				.map( pageCacheIndex::isOutdated )
				.flatMap( isOutdated -> {
					boolean networkConnected = NetworkUtils.isConnected( context );
					if ( !isOutdated )
					{
						// current version available
						return getPageFromCache( pageIdentifier, networkConnected );
					}
					else if ( networkConnected )
					{
						// download page
						return getPageFromServer( pageIdentifier, false );
					}
					else
					{
						// no network available, but an old cached version exists
						return getPageFromCache( pageIdentifier, false );
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
		String collectionUrl = PagesUrls.getCollectionUrl( config );
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
		catch ( IOException | NoAmpPagesRequestException e )
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

	/**
	 * Download collection from server.
	 * Adds collection identifier and authorization token to request.<br/>
	 * Uses default collection identifier as specified in {@link this#config}
	 */
	private Observable<Collection> getCollectionFromServer( boolean cacheAsBackup )
	{
		return ampApi.getCollection( config.collectionIdentifier, config.authorizationHeaderValue )
				.map( CollectionResponse::getCollection )
				.doOnNext( saveCollectionCacheIndex() )
				.compose( RxUtils.runOnIoThread() )
				.onErrorResumeNext( throwable -> {
					String collectionUrl = PagesUrls.getCollectionUrl( config );
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
		String pageUrl = PagesUrls.getPageUrl( config, pageIdentifier );
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
		catch ( IOException | NoAmpPagesRequestException e )
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
				.doOnNext( savePageCacheIndex() )
				.compose( RxUtils.runOnIoThread() )
				.onErrorResumeNext( throwable -> {
					String pageUrl = PagesUrls.getPageUrl( config, pageIdentifier );
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
	private Action1<Collection> saveCollectionCacheIndex()
	{
		return collection -> CollectionCacheIndex.save( config, context, collection.getLastChanged() );
	}

	@NonNull
	private Action1<Page> savePageCacheIndex()
	{
		return page -> PageCacheIndex.save( page, config, context );
	}

	public void setCollectionListener( CollectionDownloadedListener collectionListener )
	{
		this.collectionListener = collectionListener;
	}
}