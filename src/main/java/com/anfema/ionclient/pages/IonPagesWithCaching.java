package com.anfema.ionclient.pages;

import android.content.Context;

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
import com.anfema.ionclient.utils.DateTimeUtils;
import com.anfema.ionclient.utils.FileUtils;
import com.anfema.ionclient.utils.IonLog;
import com.anfema.ionclient.utils.PagesFilter;
import com.anfema.ionclient.utils.PendingDownloadHandler;
import com.anfema.utils.NetworkUtils;
import com.anfema.utils.StringUtilsKt;

import org.joda.time.DateTime;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.HttpException;
import retrofit2.Response;

import static com.anfema.ionclient.pages.RetrofitIonPagesApiKt.retrofitIonPagesApi;

/**
 * A wrapper of "collections" and "pages" call of ION API.
 * <p>
 * Adds collection identifier and authorization token to request as retrieved via {@link IonConfig}<br/>
 * <p>
 * Uses a file and a memory cache.
 */
public class IonPagesWithCaching implements IonPages
{
	public static final int                          COLLECTION_NOT_MODIFIED = 304;
	private             CollectionDownloadedListener collectionListener;

	private final PendingDownloadHandler<String, Collection> runningCollectionDownload; //key: collection identifier
	private final PendingDownloadHandler<String, Page>       runningPageDownloads; //key: page identifier

	private final Context context;

	/**
	 * Contains essential data for making calls to ION API
	 */
	private IonConfig config;

	/**
	 * Access to the ION API
	 */
	private final RetrofitIonPagesApi ionApi;

	public IonPagesWithCaching( OkHttpClient okHttpClient, IonConfig config, Context context )
	{
		this.config = config;
		this.context = context;
		ionApi = retrofitIonPagesApi( okHttpClient, config.baseUrl );
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
	public Single<Collection> fetchCollection()
	{
		return fetchCollection( false );
	}

	/**
	 * @param preferNetwork try network download as first option if set to false
	 */
	@Override
	public Single<Collection> fetchCollection( boolean preferNetwork )
	{
		// clear incompatible cache
		CacheCompatManager.cleanUp( context );

		CollectionCacheIndex cacheIndex = CollectionCacheIndex.retrieve( config, context );

		boolean currentCacheEntry = cacheIndex != null && !cacheIndex.isOutdated( config );
		boolean networkAvailable = NetworkUtils.isConnected( context ) && IonConfig.cachingStrategy != CachingStrategy.STRICT_OFFLINE;

		if ( currentCacheEntry && !preferNetwork )
		{
			// retrieve current version from cache
			return fetchCollectionFromCache( cacheIndex, networkAvailable );
		}
		else if ( networkAvailable )
		{
			// download collection
			return fetchCollectionFromServer( cacheIndex );
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
			return Single.error( new CollectionNotAvailableException() );
		}
	}

	@Override
	public Single<PagePreview> fetchPagePreview( String pageIdentifier )
	{
		return fetchCollection()
				.map( collection -> collection.pages )
				.flatMapObservable( Observable::fromIterable )
				.filter( PagesFilter.identifierEquals( pageIdentifier ) )
				.singleOrError();
	}

	@Override
	public Observable<PagePreview> fetchPagePreviews( Predicate<PagePreview> pagesFilter )
	{
		return fetchCollection()
				.map( collection -> collection.pages )
				.flatMapObservable( Observable::fromIterable )
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
	public Single<Page> fetchPage( String pageIdentifier )
	{
		// clear incompatible cache
		CacheCompatManager.cleanUp( context );

		String pageUrl = IonPageUrls.getPageUrl( config, pageIdentifier );
		PageCacheIndex pageCacheIndex = PageCacheIndex.retrieve( pageUrl, config, context );

		if ( pageCacheIndex == null )
		{
			// no cached version available: There is no need to fetch collection for date comparison.
			if ( NetworkUtils.isConnected( context ) )
			{
				// download page
				return fetchPageFromServer( pageIdentifier );
			}
			else
			{
				// page can neither be downloaded nor be found in cache
				return Single.error( new PageNotAvailableException() );
			}
		}

		return fetchCollection()
				// get page's last_changed date from collection
				.flatMap( collection -> collection.getPageLastChangedAsync( pageIdentifier ) )
				// compare last_changed date of cached page with that of collection
				.map( pageCacheIndex::isOutdated )
				.flatMap( isOutdated ->
				{
					boolean networkAvailable = NetworkUtils.isConnected( context ) && IonConfig.cachingStrategy != CachingStrategy.STRICT_OFFLINE;
					if ( !isOutdated )
					{
						// current version available
						return fetchPageFromCache( pageIdentifier, networkAvailable );
					}
					else if ( networkAvailable )
					{
						// download page
						return fetchPageFromServer( pageIdentifier );
					}
					else
					{
						// no network available, but an old cached version exists
						return fetchPageFromCache( pageIdentifier, false );
					}
				} );
	}

	/**
	 * Fetch a set of pages by passing its page identifiers.
	 * This is a convenience method for {@link #fetchPages(Predicate)}.
	 */
	@Override
	public Observable<Page> fetchPages( List<String> pageIdentifiers )
	{
		return Observable.fromIterable( pageIdentifiers )
				.concatMapSingle( this::fetchPage );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.<br/>
	 * Use collection identifier as specified in {@link this#config}
	 */
	@Override
	public Observable<Page> fetchPages( Predicate<PagePreview> pagesFilter )
	{
		return fetchCollection()
				.map( collection -> collection.pages )
				.flatMapObservable( Observable::fromIterable )
				.filter( pagesFilter )
				.map( page -> page.identifier )
				.concatMapSingle( this::fetchPage );
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

	private Single<Collection> fetchCollectionFromCache( CollectionCacheIndex cacheIndex, boolean serverCallAsBackup )
	{
		String collectionUrl = IonPageUrls.getCollectionUrl( config );

		// retrieve from memory cache
		Collection collection = MemoryCache.getCollection( collectionUrl );
		if ( collection != null )
		{
			IonLog.i( "Memory Cache Lookup", collectionUrl );
			return Single.just( collection );
		}

		// retrieve from file cache
		IonLog.i( "File Cache Lookup", collectionUrl );

		File filePath = FilePaths.getCollectionJsonPath( collectionUrl, config, context );
		if ( !filePath.exists() )
		{
			return Single.error( new CollectionNotAvailableException() );
		}

		return FileUtils.readTextFromFile( filePath )
				// deserialize collection and remember byte count
				.map( collectionsString ->
				{
					CollectionResponse collectionResponse = GsonHolder.INSTANCE.getDefaultInstance().fromJson( collectionsString, CollectionResponse.class );
					Collection collection1 = collectionResponse.getCollection();
					collection1.byteCount = StringUtilsKt.byteCount( collectionsString );
					return collection1;
				} )
				// save to memory cache
				.doOnSuccess( collection1 -> MemoryCache.saveCollection( collection1, collectionUrl, context ) )
				.onErrorResumeNext( throwable ->
						handleUnsuccessfulCollectionCacheReading( collectionUrl, cacheIndex, serverCallAsBackup, throwable ) )
				.subscribeOn( Schedulers.io() );
	}

	private Single<Collection> handleUnsuccessfulCollectionCacheReading( String collectionUrl, CollectionCacheIndex cacheIndex, boolean serverCallAsBackup, Throwable e )
	{
		if ( serverCallAsBackup )
		{
			IonLog.w( "Backup Request", "Cache lookup " + collectionUrl + " failed. Trying network request instead..." );
			IonLog.ex( "Cache Lookup", e );
			return fetchCollectionFromServer( cacheIndex );
		}
		else
		{
			IonLog.e( "Failed Request", "Cache lookup " + collectionUrl + " failed." );
			return Single.error( new ReadFromCacheException( collectionUrl, e ) );
		}
	}

	/**
	 * Download collection from server.
	 * Adds collection identifier and authorization token to request.<br/>
	 * Uses default collection identifier as specified in {@link this#config}
	 */
	private Single<Collection> fetchCollectionFromServer( CollectionCacheIndex cacheIndex )
	{
		final String lastModified = cacheIndex != null ? cacheIndex.getLastModified() : null;

		DateTime requestTime = DateTimeUtils.now();
		Single<Collection> collectionSingle = config.authenticatedRequest(
				authorizationHeaderValue -> ionApi.getCollection( config.collectionIdentifier, config.locale, authorizationHeaderValue, config.variation, lastModified ) )
				.flatMap( serverResponse ->
				{
					if ( serverResponse.code() == COLLECTION_NOT_MODIFIED )
					{
						// collection has not changed, return cached version
						return fetchCollectionFromCache( cacheIndex, false )
								// update cache index again (last updated needs to be reset to now)
								.doOnSuccess( saveCollectionCacheIndex( lastModified, requestTime ) );
					}
					else if ( serverResponse.isSuccessful() )
					{
						String lastModifiedReceived = serverResponse.headers().get( "Last-Modified" );

						// parse collection data from response and write cache index and memory cache
						return Single.just( serverResponse )
								// unwrap page and remember byte count
								.map( ( response ) ->
								{
									Collection collection = response.body().getCollection();
									collection.byteCount = getContentByteCount( response );
									return collection;
								} )
								.doOnSuccess( collection -> MemoryCache.saveCollection( collection, config, context ) )
								.doOnSuccess( saveCollectionCacheIndex( lastModifiedReceived, requestTime ) )
								.doOnSuccess( collection ->
								{
									if ( collectionListener != null )
									{
										collectionListener.collectionDownloaded( collection, lastModifiedReceived );
									}
								} );
					}
					else
					{
						return Single.error( new HttpException( serverResponse ) );
					}
				} )
				.onErrorResumeNext( throwable ->
				{
					String collectionUrl = IonPageUrls.getCollectionUrl( config );
					IonLog.e( "Failed Request", "Network request " + collectionUrl + " failed." );
					return Single.error( new NetworkRequestException( collectionUrl, throwable ) );
				} )
				.subscribeOn( Schedulers.io() )
				.doFinally( () -> runningCollectionDownload.finished( config.collectionIdentifier ) );
		return runningCollectionDownload.starting( config.collectionIdentifier, collectionSingle.toObservable() ).singleOrError();
	}


	/// Get collection methods END


	/// Get page methods

	/**
	 * @param serverCallAsBackup If reading from cache is not successful, should a server call be made?
	 */
	private Single<Page> fetchPageFromCache( String pageIdentifier, boolean serverCallAsBackup )
	{
		String pageUrl = IonPageUrls.getPageUrl( config, pageIdentifier );

		// retrieve from memory cache
		Page memPage = MemoryCache.getPage( pageUrl );
		if ( memPage != null )
		{
			IonLog.i( "Memory Cache Lookup", pageUrl );
			return Single.just( memPage );
		}

		// retrieve from file cache
		IonLog.i( "File Cache Lookup", pageUrl );

		File filePath = FilePaths.getPageJsonPath( pageUrl, pageIdentifier, config, context );
		if ( !filePath.exists() )
		{
			return handleUnsuccessfulPageCacheReading( pageIdentifier, serverCallAsBackup, pageUrl, new PageNotAvailableException() );
		}

		return FileUtils.readTextFromFile( filePath )
				// deserialize page and remember byte count
				.map( pagesString ->
				{
					PageResponse pageResponse = GsonHolder.INSTANCE.getDefaultInstance().fromJson( pagesString, PageResponse.class );
					Page page = pageResponse.getPage();
					page.byteCount = StringUtilsKt.byteCount( pagesString );
					return page;
				} )
				// save to memory cache
				.doOnSuccess( page -> MemoryCache.savePage( page, config, context ) )
				.onErrorResumeNext( throwable -> handleUnsuccessfulPageCacheReading( pageIdentifier, serverCallAsBackup, pageUrl, throwable ) )
				.subscribeOn( Schedulers.io() );
	}

	private Single<Page> handleUnsuccessfulPageCacheReading( String pageIdentifier, boolean serverCallAsBackup, String pageUrl, Throwable e )
	{
		if ( serverCallAsBackup )
		{
			IonLog.w( "Backup Request", "Cache lookup " + pageUrl + " failed. Trying network request instead..." );
			IonLog.ex( "Cache Lookup", e );
			return fetchPageFromServer( pageIdentifier );
		}
		IonLog.e( "Failed Request", "Cache lookup " + pageUrl + " failed." );
		return Single.error( new ReadFromCacheException( pageUrl, e ) );
	}

	private Single<Page> fetchPageFromServer( String pageIdentifier )
	{
		Single<Page> pageSingle = config.authenticatedRequest(
				authorizationHeaderValue -> ionApi.getPage( config.collectionIdentifier, pageIdentifier, config.locale, config.variation, authorizationHeaderValue ) )
				// unwrap page and remember byte count
				.map( response ->
				{
					if ( response.isSuccessful() )
					{
						Page page = response.body().getPage();
						page.byteCount = getContentByteCount( response );
						return page;
					}
					else
					{
						throw new HttpException( response );
					}
				} )
				.doOnSuccess( savePageCacheIndex() )
				.doOnSuccess( page -> MemoryCache.savePage( page, config, context ) )
				.onErrorResumeNext( throwable ->
				{
					String pageUrl = IonPageUrls.getPageUrl( config, pageIdentifier );
					IonLog.e( "Failed Request", "Network request " + pageUrl + " failed." );
					return Single.error( new NetworkRequestException( pageUrl, throwable ) );
				} )
				.subscribeOn( Schedulers.io() )
				.doFinally( () -> runningPageDownloads.finished( pageIdentifier ) );
		return runningPageDownloads.starting( pageIdentifier, pageSingle.toObservable() ).singleOrError();
	}

	/**
	 * Find out size of response body and calculate how much space it takes in a Java string,
	 * or - if header is not set - (conservatively) assume it is 100 KB.
	 *
	 * @return unit: bytes
	 */
	private static int getContentByteCount( Response<?> response )
	{
		String contentLength = response.headers().get( "Content-Length" );
		return contentLength != null ? Integer.parseInt( contentLength ) * 2 : 100 * 1024;
	}

	/// Get page methods END


	@NonNull
	private Consumer<Collection> saveCollectionCacheIndex( String lastModified, DateTime lastUpdated )
	{
		return collection -> CollectionCacheIndex.save( config, context, lastModified, lastUpdated );
	}

	@NonNull
	private Consumer<Page> savePageCacheIndex()
	{
		return page -> PageCacheIndex.save( page, config, context );
	}

	public void setCollectionListener( CollectionDownloadedListener collectionListener )
	{
		this.collectionListener = collectionListener;
	}
}
