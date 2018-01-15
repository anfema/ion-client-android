package com.anfema.ionclient;

import android.content.Context;
import android.widget.ImageView;

import com.anfema.ionclient.archive.IonArchive;
import com.anfema.ionclient.archive.IonArchiveFactory;
import com.anfema.ionclient.fulltextsearch.IonFts;
import com.anfema.ionclient.fulltextsearch.IonFtsFactory;
import com.anfema.ionclient.fulltextsearch.SearchResult;
import com.anfema.ionclient.mediafiles.FileWithStatus;
import com.anfema.ionclient.mediafiles.IonFiles;
import com.anfema.ionclient.mediafiles.IonFilesFactory;
import com.anfema.ionclient.mediafiles.IonPicasso;
import com.anfema.ionclient.mediafiles.IonPicassoFactory;
import com.anfema.ionclient.pages.IonPages;
import com.anfema.ionclient.pages.IonPagesFactory;
import com.anfema.ionclient.pages.models.Collection;
import com.anfema.ionclient.pages.models.Page;
import com.anfema.ionclient.pages.models.PagePreview;
import com.anfema.ionclient.pages.models.contents.Downloadable;
import com.anfema.ionclient.utils.ContextUtils;
import com.anfema.ionclient.utils.IonLog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import okhttp3.HttpUrl;

/**
 * Main entry point for ION functionality. Obtain an instance with {@link #getInstance(IonConfig, Context)}.
 * <p>
 * Serving as entry point IonClient holds interfaces providing the actual implementation of its functionality.
 */
public class IonClient implements IonPages, IonFiles, IonPicasso, IonArchive, IonFts
{
	/// Multiton

	private static Map<IonConfig, IonClient> instances = new HashMap<>();

	/**
	 * @param config configuration for ION client
	 * @return client instance, ready to go
	 */
	public static IonClient getInstance( IonConfig config, Context context )
	{
		IonConfig.assertConfigIsValid( config );

		// check if client for this configuration already exists, otherwise create an instance
		IonClient storedClient = instances.get( config );
		if ( storedClient != null && storedClient.context != null )
		{
			// update config because values, which are not included in equality check, might have changed
			storedClient.updateConfig( config );
			return storedClient;
		}

		context = ContextUtils.getApplicationContext( context );
		IonClient ionClient = new IonClient( config, context );
		instances.put( config, ionClient );
		IonLog.d( "IonClient", "# ION client instances: " + instances.size() );
		return ionClient;
	}

	/// Multiton END


	// stored to verify on #getInstance(IonConfig, Context) that context (which is passed to delegate classes) is not null.
	private Context context;

	// delegate classes
	private final IonPages   ionPages;
	private final IonFiles   ionFiles;
	private final IonPicasso ionPicasso;
	private final IonArchive ionArchive;
	private final IonFts     ionFts;

	private IonClient( IonConfig config, Context context )
	{
		this.context = context;
		ionPages = IonPagesFactory.newInstance( config, context );
		ionFiles = IonFilesFactory.newInstance( config, context );
		ionPicasso = IonPicassoFactory.newInstance( ionFiles, config, context );
		ionArchive = IonArchiveFactory.newInstance( ionPages, ionFiles, config, context );
		ionFts = IonFtsFactory.newInstance( ionPages, ionFiles, config, context );
	}

	@Override
	public void updateConfig( IonConfig config )
	{
		ionPages.updateConfig( config );
		ionFiles.updateConfig( config );
		ionPicasso.updateConfig( config );
		ionArchive.updateConfig( config );
		ionFts.updateConfig( config );
	}

	/// Collection and page calls

	/**
	 * Call collections on Ion API.
	 * Adds collection identifier and authorization token to request as retrieved via {@link IonConfig}<br/>
	 */
	@Override
	public Single<Collection> fetchCollection()
	{
		return ionPages.fetchCollection();
	}


	@Override
	public Single<PagePreview> fetchPagePreview( String pageIdentifier )
	{
		return ionPages.fetchPagePreview( pageIdentifier );
	}

	/**
	 * A set of page previews is "returned" by emitting multiple events.
	 *
	 * @param pagesFilter see {@link com.anfema.ionclient.utils.PagesFilter} for some frequently used filter options.
	 */
	@Override
	public Observable<PagePreview> fetchPagePreviews( Predicate<PagePreview> pagesFilter )
	{
		return ionPages.fetchPagePreviews( pagesFilter );
	}

	@Override
	public Observable<PagePreview> fetchAllPagePreviews()
	{
		return ionPages.fetchAllPagePreviews();
	}

	/**
	 * Add collection identifier and authorization token to request.<br/>
	 */
	@Override
	public Single<Page> fetchPage( String pageIdentifier )
	{
		return ionPages.fetchPage( pageIdentifier );
	}

	@Override
	public Observable<Page> fetchPages( List<String> pageIdentifiers )
	{
		return ionPages.fetchPages( pageIdentifiers );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.<br/>
	 *
	 * @param pagesFilter see {@link com.anfema.ionclient.utils.PagesFilter} for some frequently used filter options.
	 */
	@Override
	public Observable<Page> fetchPages( Predicate<PagePreview> pagesFilter )
	{
		return ionPages.fetchPages( pagesFilter );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.<br/>
	 */
	@Override
	public Observable<Page> fetchAllPages()
	{
		return ionPages.fetchAllPages();
	}


	// Loading media files

	/**
	 * @see IonFiles#request(Downloadable)
	 */
	@Override
	public Single<FileWithStatus> request( Downloadable content )
	{
		return ionFiles.request( content );
	}

	/**
	 * @see IonFiles#request(HttpUrl, String)
	 */
	@Override
	public Single<FileWithStatus> request( HttpUrl url, String checksum )
	{
		return ionFiles.request( url, checksum );
	}

	/**
	 * @see IonFiles#request(HttpUrl, String, boolean, File)
	 */
	@Override
	public Single<FileWithStatus> request( HttpUrl url, String checksum, boolean ignoreCaching, File targetFile )
	{
		return ionFiles.request( url, checksum, ignoreCaching, targetFile );
	}

	@Override
	public void loadImage( int resourceId, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation )
	{
		ionPicasso.loadImage( resourceId, target, requestTransformation );
	}

	@Override
	public void loadImage( int resourceId, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation, Callback callback )
	{
		ionPicasso.loadImage( resourceId, target, requestTransformation, callback );
	}

	@Override
	public void loadImage( String path, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation )
	{
		ionPicasso.loadImage( path, target, requestTransformation );
	}

	@Override
	public void loadImage( String path, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation, Callback callback )
	{
		ionPicasso.loadImage( path, target, requestTransformation, callback );
	}

	@Override
	public void loadImage( Downloadable image, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation )
	{
		ionPicasso.loadImage( image, target, requestTransformation );
	}

	@Override
	public void loadImage( Downloadable image, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation, Callback callback )
	{
		ionPicasso.loadImage( image, target, requestTransformation, callback );
	}

	@Override
	public Picasso getPicassoInstance()
	{
		return ionPicasso.getPicassoInstance();
	}

	@Override
	public Single<Picasso> getPicassoInstanceDoAuthCall()
	{
		return ionPicasso.getPicassoInstanceDoAuthCall();
	}


	/// Archive download

	/**
	 * @see IonArchive#downloadArchive()
	 */
	@Override
	public Completable downloadArchive()
	{
		return ionArchive.downloadArchive();
	}


	/// Full text search

	/**
	 * @see IonFts#downloadSearchDatabase()
	 */
	@Override
	public Completable downloadSearchDatabase()
	{
		return ionFts.downloadSearchDatabase();
	}

	/**
	 * @see IonFts#fullTextSearch(String, String, String)
	 */
	@Override
	public Single<List<SearchResult>> fullTextSearch( String searchTerm, String locale, String pageLayout )
	{
		return ionFts.fullTextSearch( searchTerm, locale, pageLayout );
	}
}
