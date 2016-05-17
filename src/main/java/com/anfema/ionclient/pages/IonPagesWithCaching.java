package com.anfema.ionclient.pages;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.IonConfig.CachingStrategy;
import com.anfema.ionclient.caching.CacheCompatManager;
import com.anfema.ionclient.caching.FilePaths;
import com.anfema.ionclient.caching.MemoryCache;
import com.anfema.ionclient.caching.index.CollectionCacheIndex;
import com.anfema.ionclient.caching.index.PageCacheIndex;
import com.anfema.ionclient.exceptions.CollectionNotAvailableException;
import com.anfema.ionclient.exceptions.NetworkRequestException;
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
import com.anfema.ionclient.utils.IonLog;
import com.anfema.ionclient.utils.PagesFilter;
import com.anfema.ionclient.utils.PendingDownloadHandler;
import com.anfema.ionclient.utils.RxUtils;
import com.anfema.utils.NetworkUtils;

import java.io.File;
import java.util.List;

import okhttp3.Interceptor;
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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

	private PendingDownloadHandler<String, Collection> runningCollectionDownload; //key: collection identifier
	private PendingDownloadHandler<String, Page>       runningPageDownloads; //key: page identifier

	private final Context context;

	/**
	 * Contains essential data for making calls to ION API
	 */
	private IonConfig config;

	/**
	 * Access to the ION API
	 */
	private final IonPagesApi ionApi;

	public IonPagesWithCaching( IonConfig config, Context context, List<Interceptor> interceptors )
	{
		this.config = config;
		this.context = context;
		ionApi = ApiFactory.newInstance( config.baseUrl, interceptors, IonPagesApi.class );
		runningCollectionDownload = new PendingDownloadHandler<>();
		runningPageDownloads = new PendingDownloadHandler<>();
	}

	@Override
	public void updateConfig( IonConfig config )
	{
		this.config = config;
	}

	/**
	 * Retrieve collection. Strategy depends on {@link IonConfig#cachingStrategy}.
	 * Use default collection identifier as specified in {@link #config}
	 */
	@Override
	public Observable<Collection> fetchCollection()
	{
		// clear incompatible cache
		CacheCompatManager.cleanUp( context );

		CollectionCacheIndex cacheIndex = CollectionCacheIndex.retrieve( config, context );

		boolean currentCacheEntry = cacheIndex != null && !cacheIndex.isOutdated( config );
		boolean networkAvailable = NetworkUtils.isConnected( context ) && IonConfig.cachingStrategy != CachingStrategy.STRICT_OFFLINE;

		if ( currentCacheEntry )
		{
			// retrieve current version from cache
			return fetchCollectionFromCache( cacheIndex, networkAvailable );
		}
		else if ( networkAvailable )
		{
			// download collection
			return fetchCollectionFromServer( cacheIndex, false );
		}
		else if ( cacheIndex != null )
		{
			// no network: use old version from cache
			// TODO notify app that data might be outdated
			return fetchCollectionFromCache( cacheIndex, false );
		}
		else
		{
			// collection can neither be downloaded nor be found in cache
			return Observable.error( new CollectionNotAvailableException() );
		}
	}

	@Override
	public Observable<PagePreview> fetchPagePreview( String pageIdentifier )
	{
		return fetchCollection()
				.map( collection -> collection.pages )
				.flatMap( Observable::from )
				.filter( PagesFilter.identifierEquals( pageIdentifier ) );
	}

	@Override
	public Observable<PagePreview> fetchPagePreviews( Func1<PagePreview, Boolean> pagesFilter )
	{
		return fetchCollection()
				.map( collection -> collection.pages )
				.concatMap( Observable::from )
				.filter( pagesFilter );
	}

	@Override
	public Observable<PagePreview> fetchAllPagePreviews()
	{
		return fetchPagePreviews( PagesFilter.ALL );
	}

	/**
	 * Retrieve page with following priorities:
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
	public Observable<Page> fetchPage( String pageIdentifier )
	{
		String pageUrl = IonPageUrls.getPageUrl( config, pageIdentifier );
		PageCacheIndex pageCacheIndex = PageCacheIndex.retrieve( pageUrl, config, context );

		if ( pageCacheIndex == null )
		{
			// no cached version available: There is no need to fetch collection for date comparison.
			if ( NetworkUtils.isConnected( context ) )
			{
				// download page
				return fetchPageFromServer( pageIdentifier, false );
			}
			else
			{
				// page can neither be downloaded nor be found in cache
				return Observable.error( new PageNotAvailableException() );
			}
		}

		return fetchCollection()
				.observeOn( Schedulers.io() )
				// get page's last_changed date from collection
				.flatMap( collection -> collection.getPageLastChangedAsync( pageIdentifier ) )
				// compare last_changed date of cached page with that of collection
				.map( pageCacheIndex::isOutdated )
				.flatMap( isOutdated -> {
					boolean networkAvailable = NetworkUtils.isConnected( context ) && IonConfig.cachingStrategy != CachingStrategy.STRICT_OFFLINE;
					if ( !isOutdated )
					{
						// current version available
						return fetchPageFromCache( pageIdentifier, networkAvailable );
					}
					else if ( networkAvailable )
					{
						// download page
						return fetchPageFromServer( pageIdentifier, false );
					}
					else
					{
						// no network available, but an old cached version exists
						return fetchPageFromCache( pageIdentifier, false );
					}
				} )
				.observeOn( AndroidSchedulers.mainThread() );
	}

	/**
	 * Fetch a set of pages by passing its page identifiers.
	 * This is a convenience method for {@link #fetchPages(Func1)}.
	 */
	@Override
	public Observable<Page> fetchPages( List<String> pageIdentifiers )
	{
		return fetchPages( PagesFilter.identifierIn( pageIdentifiers ) );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.<br/>
	 * Use collection identifier as specified in {@link this#config}
	 */
	@Override
	public Observable<Page> fetchPages( Func1<PagePreview, Boolean> pagesFilter )
	{
		return fetchCollection()
				.observeOn( Schedulers.io() )
				.map( collection -> collection.pages )
				.concatMap( Observable::from )
				.filter( pagesFilter )
				.map( page -> page.identifier )
				.concatMap( this::fetchPage )
				.observeOn( AndroidSchedulers.mainThread() );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.<br/>
	 * Use collection identifier as specified in {@link this#config}
	 */
	@Override
	public Observable<Page> fetchAllPages()
	{
		return fetchPages( PagesFilter.ALL );
	}


	/// Get collection methods

	private Observable<Collection> fetchCollectionFromCache( CollectionCacheIndex cacheIndex, boolean serverCallAsBackup )
	{
		String collectionUrl = IonPageUrls.getCollectionUrl( config );

		// retrieve from memory cache
		Collection collection = MemoryCache.getCollection( collectionUrl );
		if ( collection != null )
		{
			IonLog.i( "Memory Cache Lookup", collectionUrl );
			return Observable.just( collection );
		}

		// retrieve from file cache
		IonLog.i( "File Cache Lookup", collectionUrl );

		File filePath = FilePaths.getCollectionJsonPath( collectionUrl, config, context );
		if ( !filePath.exists() )
		{
			return Observable.error( new CollectionNotAvailableException() );
		}

		return FileUtils.readTextFromFile( filePath )
				.map( collectionsString -> GsonHolder.getInstance().fromJson( collectionsString, CollectionResponse.class ) )
				.map( CollectionResponse::getCollection )
				// save to memory cache
				.doOnNext( collection1 -> MemoryCache.saveCollection( collection1, collectionUrl ) )
				.onErrorResumeNext( throwable -> {
					return handleUnsuccessfulCollectionCacheReading( collectionUrl, cacheIndex, serverCallAsBackup, throwable );
				} )
				.compose( RxUtils.runOnIoThread() );
	}

	private Observable<Collection> handleUnsuccessfulCollectionCacheReading( String collectionUrl, CollectionCacheIndex cacheIndex, boolean serverCallAsBackup, Throwable e )
	{
		if ( serverCallAsBackup )
		{
			IonLog.w( "Backup Request", "Cache lookup " + collectionUrl + " failed. Trying network request instead..." );
			IonLog.ex( "Cache Lookup", e );
			return fetchCollectionFromServer( cacheIndex, false );
		}
		else
		{
			IonLog.e( "Failed Request", "Cache lookup " + collectionUrl + " failed." );
			return Observable.error( new ReadFromCacheException( collectionUrl, e ) );
		}
	}

	/**
	 * Download collection from server.
	 * Adds collection identifier and authorization token to request.<br/>
	 * Uses default collection identifier as specified in {@link this#config}
	 */
	private Observable<Collection> fetchCollectionFromServer( CollectionCacheIndex cacheIndex, boolean cacheAsBackup )
	{
		final String lastModified = cacheIndex != null ? cacheIndex.getLastModified() : null;

		Observable<Collection> collectionObservable = config.authenticatedRequest(
				authorizationHeaderValue -> ionApi.getCollection( config.collectionIdentifier, config.locale, authorizationHeaderValue, config.variation, lastModified ) )
				.flatMap( serverResponse -> {
					if ( serverResponse.code() == COLLECTION_NOT_MODIFIED )
					{
						// collection has not changed, return cached version
						return fetchCollectionFromCache( cacheIndex, false )
								// update cache index again (last updated needs to be reset to now)
								.doOnNext( saveCollectionCacheIndex( lastModified ) );
					}
					else if ( serverResponse.isSuccessful() )
					{
						String lastModifiedReceived = serverResponse.headers().get( "Last-Modified" );

						// parse collection data from response and write cache index and memory cache
						return Observable.just( serverResponse.body() )
								.map( CollectionResponse::getCollection )
								.doOnNext( collection1 -> MemoryCache.saveCollection( collection1, config ) )
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
				.onErrorResumeNext( throwable -> {
					String collectionUrl = IonPageUrls.getCollectionUrl( config );
					if ( cacheAsBackup )
					{
						IonLog.w( "Backup Request", "Network request " + collectionUrl + " failed. Trying cache request instead..." );
						IonLog.ex( "Network Request", throwable );
						return fetchCollectionFromCache( cacheIndex, false );
					}
					IonLog.e( "Failed Request", "Network request " + collectionUrl + " failed." );
					return Observable.error( new NetworkRequestException( collectionUrl, throwable ) );
				} )
				.compose( RxUtils.runOnIoThread() )
				.doOnNext( file -> runningCollectionDownload.finished( config.collectionIdentifier ) );
		return runningCollectionDownload.starting( config.collectionIdentifier, collectionObservable );
	}


	/// Get collection methods END


	/// Get page methods

	/**
	 * @param serverCallAsBackup If reading from cache is not successful, should a server call be made?
	 */
	private Observable<Page> fetchPageFromCache( String pageIdentifier, boolean serverCallAsBackup )
	{
		// clear incompatible cache
		CacheCompatManager.cleanUp( context );

		String pageUrl = IonPageUrls.getPageUrl( config, pageIdentifier );

		// retrieve from memory cache
		Page memPage = MemoryCache.getPage( pageUrl );
		if ( memPage != null )
		{
			IonLog.i( "Memory Cache Lookup", pageUrl );
			return Observable.just( memPage );
		}

		// retrieve from file cache
		IonLog.i( "File Cache Lookup", pageUrl );

		File filePath = FilePaths.getPageJsonPath( pageUrl, pageIdentifier, config, context );
		if ( !filePath.exists() )
		{
			return Observable.error( new PageNotAvailableException() );
		}

		return FileUtils.readTextFromFile( filePath )
				.map( pagesString -> GsonHolder.getInstance().fromJson( pagesString, PageResponse.class ) )
				.map( PageResponse::getPage )
				// save to memory cache
				.doOnNext( page -> MemoryCache.savePage( page, config ) )
				.onErrorResumeNext( throwable -> {
					return handleUnsuccessfulPageCacheReading( pageIdentifier, serverCallAsBackup, pageUrl, throwable );
				} )
				.compose( RxUtils.runOnIoThread() );
	}

	private Observable<Page> handleUnsuccessfulPageCacheReading( String pageIdentifier, boolean serverCallAsBackup, String pageUrl, Throwable e )
	{
		if ( serverCallAsBackup )
		{
			IonLog.w( "Backup Request", "Cache lookup " + pageUrl + " failed. Trying network request instead..." );
			IonLog.ex( "Cache Lookup", e );
			return fetchPageFromServer( pageIdentifier, false );
		}
		IonLog.e( "Failed Request", "Cache lookup " + pageUrl + " failed." );
		return Observable.error( new ReadFromCacheException( pageUrl, e ) );
	}

	/**
	 * @param cacheAsBackup If server call is not successful, should cached version be used (even if it might be old)?
	 */
	private Observable<Page> fetchPageFromServer( String pageIdentifier, boolean cacheAsBackup )
	{
		Observable<Page> pageObservable = config.authenticatedRequest(
				authorizationHeaderValue -> ionApi.getPage( config.collectionIdentifier, pageIdentifier, config.locale, config.variation, authorizationHeaderValue ) )
				.map( Response::body )
				.map( PageResponse::getPage )
				.doOnNext( savePageCacheIndex() )
				.doOnNext( page -> MemoryCache.savePage( page, config ) )
				.onErrorResumeNext( throwable -> {
					String pageUrl = IonPageUrls.getPageUrl( config, pageIdentifier );
					if ( cacheAsBackup )
					{
						IonLog.w( "Backup Request", "Network request " + pageUrl + " failed. Trying cache request instead..." );
						IonLog.ex( "Network Request", throwable );
						return fetchPageFromCache( pageIdentifier, false );
					}
					IonLog.e( "Failed Request", "Network request " + pageUrl + " failed." );
					return Observable.error( new NetworkRequestException( pageUrl, throwable ) );
				} )
				.compose( RxUtils.runOnIoThread() )
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
}