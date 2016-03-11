package com.anfema.ionclient.pages;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.caching.CollectionCacheIndex;
import com.anfema.ionclient.caching.FilePaths;
import com.anfema.ionclient.caching.MemoryCache;
import com.anfema.ionclient.caching.PageCacheIndex;
import com.anfema.ionclient.exceptions.CollectionNotAvailableException;
import com.anfema.ionclient.exceptions.NetworkRequestException;
import com.anfema.ionclient.exceptions.NoIonPagesRequestException;
import com.anfema.ionclient.exceptions.PageNotAvailableException;
import com.anfema.ionclient.exceptions.ReadFromCacheException;
import com.anfema.ionclient.pages.models.Collection;
import com.anfema.ionclient.pages.models.Page;
import com.anfema.ionclient.pages.models.PagePreview;
import com.anfema.ionclient.pages.models.responses.CollectionResponse;
import com.anfema.ionclient.pages.models.responses.PageResponse;
import com.anfema.ionclient.serialization.GsonHolder;
import com.anfema.ionclient.utils.ApiFactory;
import com.anfema.ionclient.utils.FileUtils;
import com.anfema.ionclient.utils.Log;
import com.anfema.ionclient.utils.NetworkUtils;
import com.anfema.ionclient.utils.PagesFilter;
import com.anfema.ionclient.utils.RunningDownloadHandler;
import com.anfema.ionclient.utils.RxUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import retrofit2.HttpException;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * A wrapper of "collections" and "pages" call of ION API.
 * <p>
 * Adds collection identifier and authorization token to request as retrieved via {@link IonConfig}<br/>
 * <p>
 * Uses a file and a memory cache.
 */
public class IonPagesWithCaching implements IonPages
{
	public static final int COLLECTION_NOT_MODIFIED = 304;
	private CollectionDownloadedListener collectionListener;

	private RunningDownloadHandler<String, Collection> runningCollectionDownload; //key: collection identifier
	private RunningDownloadHandler<String, Page>       runningPageDownloads; //key: page identifier

	private final Context context;

	/**
	 * Contains essential data for making calls to ION API
	 */
	private IonConfig config;

	/**
	 * Access to the ION API
	 */
	private final IonPagesApi ionApi;

	private final MemoryCache memoryCache;

	public IonPagesWithCaching( IonConfig config, Context context, List<Interceptor> interceptors )
	{
		this.config = config;
		this.context = context;
		ionApi = ApiFactory.newInstance( config.baseUrl, interceptors, IonPagesApi.class );
		memoryCache = new MemoryCache();
		runningCollectionDownload = new RunningDownloadHandler<>();
		runningPageDownloads = new RunningDownloadHandler<>();
	}

	@Override
	public void updateConfig( IonConfig config )
	{
		this.config = config;
	}

	/**
	 * Retrieve collection with following priorities:
	 * <p>
	 * 1. Look if there is a current version in cache
	 * 2. Download from server if internet connection available
	 * 3. If no internet connection: return cached version (even if outdated)
	 * 4. Collection is not retrievable at all: emit error
	 * <p>
	 * Use default collection identifier as specified in {@link this#config}
	 */
	@Override
	public Observable<Collection> getCollection()
	{
		String collectionUrl = IonPageUrls.getCollectionUrl( config );
		CollectionCacheIndex cacheIndex = CollectionCacheIndex.retrieve( collectionUrl, config.collectionIdentifier, context );

		boolean currentCacheEntry = cacheIndex != null && !cacheIndex.isOutdated( config );
		boolean networkConnected = NetworkUtils.isConnected( context );

		if ( currentCacheEntry )
		{
			// retrieve current version from cache
			return getCollectionFromCache( cacheIndex, networkConnected );
		}
		else if ( networkConnected )
		{
			// download collection
			return getCollectionFromServer( cacheIndex, false );
		}
		else if ( cacheIndex != null )
		{
			// no network: use old version from cache
			// TODO notify app that data might be outdated
			return getCollectionFromCache( cacheIndex, false );
		}
		else
		{
			// collection can neither be downloaded nor be found in cache
			return Observable.error( new CollectionNotAvailableException() );
		}
	}

	/**
	 * Retrieve page with following priorities:
	 * <p>
	 * <p>
	 * 1. Look if there is a current version in cache
	 * 2. Download from server if internet connection available
	 * 3. If no internet connection: return cached version (even if outdated)
	 * 4. Collection is not retrievable at all: emit error
	 * <p>
	 * Add collection identifier and authorization token to request.<br/>
	 * Use default collection identifier as specified in {@link this#config}
	 */
	@Override
	public Observable<Page> getPage( String pageIdentifier )
	{
		String pageUrl = IonPageUrls.getPageUrl( config, pageIdentifier );
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
	public Observable<Page> getPagesSorted( Func1<PagePreview, Boolean> pagesFilter )
	{
		return getCollection()
				.map( collection -> collection.pages )
				.concatMap( Observable::from )
				.filter( pagesFilter )
				.map( page -> page.identifier )
				.concatMap( this::getPage );
	}

	@Override
	public Observable<PagePreview> getPagePreviews( Func1<PagePreview, Boolean> pagesFilter )
	{
		return getCollection()
				.map( collection -> collection.pages )
				.flatMap( Observable::from )
				.filter( pagesFilter );
	}

	@Override
	public Observable<PagePreview> getPagePreviewsSorted( Func1<PagePreview, Boolean> pagesFilter )
	{
		return getCollection()
				.map( collection -> collection.pages )
				.concatMap( Observable::from )
				.filter( pagesFilter );
	}

	@Override
	public Observable<PagePreview> getPagePreview( String pageIdentifier )
	{
		return getCollection()
				.map( collection -> collection.pages )
				.flatMap( Observable::from )
				.filter( PagesFilter.identifierEquals( pageIdentifier ) );
	}


	/// Get collection methods

	private Observable<Collection> getCollectionFromCache( CollectionCacheIndex cacheIndex, boolean serverCallAsBackup )
	{
		String collectionUrl = IonPageUrls.getCollectionUrl( config );

		// retrieve from memory cache
		Collection collection = memoryCache.getCollection();
		if ( collection != null )
		{
			Log.i( "Memory Cache Lookup", collectionUrl );
			return Observable.just( collection );
		}

		// retrieve from file cache
		Log.i( "File Cache Lookup", collectionUrl );
		try
		{
			File filePath = FilePaths.getCollectionJsonPath( collectionUrl, config, context );
			return FileUtils
					.readTextFromFile( filePath )
					.map( collectionsString -> GsonHolder.getInstance().fromJson( collectionsString, CollectionResponse.class ) )
					.map( CollectionResponse::getCollection )
					// save to memory cache
					.doOnNext( memoryCache::setCollection )
					.compose( RxUtils.runOnIoThread() )
					.onErrorResumeNext( throwable -> {
						return handleUnsuccessfulCollectionCacheReading( collectionUrl, cacheIndex, serverCallAsBackup, throwable );
					} );
		}
		catch ( IOException | NoIonPagesRequestException e )
		{
			return handleUnsuccessfulCollectionCacheReading( collectionUrl, cacheIndex, serverCallAsBackup, e );
		}
	}

	private Observable<Collection> handleUnsuccessfulCollectionCacheReading( String collectionUrl, CollectionCacheIndex cacheIndex, boolean serverCallAsBackup, Throwable e )
	{
		if ( serverCallAsBackup )
		{
			Log.w( "Backup Request", "Cache lookup " + collectionUrl + " failed. Trying network request instead..." );
			Log.ex( "Cache Lookup", e );
			return getCollectionFromServer( cacheIndex, false );
		}
		else
		{
			Log.e( "Failed Request", "Cache lookup " + collectionUrl + " failed." );
			return Observable.error( new ReadFromCacheException( collectionUrl, e ) );
		}
	}

	/**
	 * Download collection from server.
	 * Adds collection identifier and authorization token to request.<br/>
	 * Uses default collection identifier as specified in {@link this#config}
	 */
	private Observable<Collection> getCollectionFromServer( CollectionCacheIndex cacheIndex, boolean cacheAsBackup )
	{
		final String lastModified = cacheIndex != null ? cacheIndex.getLastModified() : null;

		Observable<Collection> collectionObservable = ionApi.getCollection( config.collectionIdentifier, config.locale, config.authorizationHeaderValue, config.variation, lastModified )
				.flatMap( serverResponse -> {
					if ( serverResponse.code() == COLLECTION_NOT_MODIFIED )
					{
						// collection has not changed, return cached version
						return getCollectionFromCache( cacheIndex, false )
								// update cache index again (last updated needs to be reset to now)
								.doOnNext( saveCollectionCacheIndex( lastModified ) );
					}
					else if ( serverResponse.isSuccess() )
					{
						String lastModifiedReceived = serverResponse.headers().get( "Last-Modified" );

						// parse collection data from response and write cache index and memory cache
						return Observable.just( serverResponse.body() )
								.map( CollectionResponse::getCollection )
								.doOnNext( memoryCache::setCollection )
								.doOnNext( saveCollectionCacheIndex( lastModifiedReceived ) )
								.doOnNext( collection -> {
									if ( collectionListener != null )
									{
										collectionListener.collectionDownloaded( collection, lastModifiedReceived );
									}
								} );
					}
					else
					{
						return Observable.error( new HttpException( serverResponse ) );
					}
				} )
				.compose( RxUtils.runOnIoThread() )
				.onErrorResumeNext( throwable -> {
					String collectionUrl = IonPageUrls.getCollectionUrl( config );
					if ( cacheAsBackup )
					{
						Log.w( "Backup Request", "Network request " + collectionUrl + " failed. Trying cache request instead..." );
						Log.ex( "Network Request", throwable );
						return getCollectionFromCache( cacheIndex, false );
					}
					Log.e( "Failed Request", "Network request " + collectionUrl + " failed." );
					return Observable.error( new NetworkRequestException( collectionUrl, throwable ) );
				} )
				.doOnNext( file -> runningCollectionDownload.finished( config.collectionIdentifier ) );
		return runningCollectionDownload.starting( config.collectionIdentifier, collectionObservable );
	}


	/// Get collection methods END


	/// Get page methods

	/**
	 * @param serverCallAsBackup If reading from cache is not successful, should a server call be made?
	 */
	private Observable<Page> getPageFromCache( String pageIdentifier, boolean serverCallAsBackup )
	{
		String pageUrl = IonPageUrls.getPageUrl( config, pageIdentifier );

		// retrieve from memory cache
		Page memPage = memoryCache.getPage( pageUrl );
		if ( memPage != null )
		{
			Log.i( "Memory Cache Lookup", pageUrl );
			return Observable.just( memPage );
		}

		// retrieve from file cache
		Log.i( "File Cache Lookup", pageUrl );
		try
		{
			File filePath = FilePaths.getPageJsonPath( pageUrl, pageIdentifier, config, context );
			return FileUtils
					.readTextFromFile( filePath )
					.map( pagesString -> GsonHolder.getInstance().fromJson( pagesString, PageResponse.class ) )
					.map( PageResponse::getPage )
					// save to memory cache
					.doOnNext( page -> memoryCache.savePage( page, config ) )
					.compose( RxUtils.runOnIoThread() )
					.onErrorResumeNext( throwable -> {
						return handleUnsuccessfulPageCacheReading( pageIdentifier, serverCallAsBackup, pageUrl, throwable );
					} );
		}
		catch ( IOException | NoIonPagesRequestException e )
		{
			return handleUnsuccessfulPageCacheReading( pageIdentifier, serverCallAsBackup, pageUrl, e );
		}
	}

	private Observable<Page> handleUnsuccessfulPageCacheReading( String pageIdentifier, boolean serverCallAsBackup, String pageUrl, Throwable e )
	{
		if ( serverCallAsBackup )
		{
			Log.w( "Backup Request", "Cache lookup " + pageUrl + " failed. Trying network request instead..." );
			Log.ex( "Cache Lookup", e );
			return getPageFromServer( pageIdentifier, false );
		}
		Log.e( "Failed Request", "Cache lookup " + pageUrl + " failed." );
		return Observable.error( new ReadFromCacheException( pageUrl, e ) );
	}

	/**
	 * @param cacheAsBackup If server call is not successful, should cached version be used (even if it might be old)?
	 */
	private Observable<Page> getPageFromServer( String pageIdentifier, boolean cacheAsBackup )
	{
		Observable<Page> pageObservable = ionApi.getPage( config.collectionIdentifier, pageIdentifier, config.locale, config.variation, config.authorizationHeaderValue )
				.map( PageResponse::getPage )
				.doOnNext( savePageCacheIndex() )
				.doOnNext( page -> memoryCache.savePage( page, config ) )
				.compose( RxUtils.runOnIoThread() )
				.onErrorResumeNext( throwable -> {
					String pageUrl = IonPageUrls.getPageUrl( config, pageIdentifier );
					if ( cacheAsBackup )
					{
						Log.w( "Backup Request", "Network request " + pageUrl + " failed. Trying cache request instead..." );
						Log.ex( "Network Request", throwable );
						return getPageFromCache( pageIdentifier, false );
					}
					Log.e( "Failed Request", "Network request " + pageUrl + " failed." );
					return Observable.error( new NetworkRequestException( pageUrl, throwable ) );
				} )
				.doOnNext( file -> runningPageDownloads.finished( pageIdentifier ) );
		return runningPageDownloads.starting( pageIdentifier, pageObservable );
	}

	/// Get page methods END


	@NonNull
	private Action1<Collection> saveCollectionCacheIndex( String lastModified )
	{
		return collection -> CollectionCacheIndex.save( config, context, lastModified );
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

	public MemoryCache getMemoryCache()
	{
		return memoryCache;
	}
}