package com.anfema.ampclient;

import android.content.Context;
import android.net.Uri;

import com.anfema.ampclient.archive.AmpArchive;
import com.anfema.ampclient.archive.AmpArchiveFactory;
import com.anfema.ampclient.fulltextsearch.AmpFts;
import com.anfema.ampclient.fulltextsearch.AmpFtsFactory;
import com.anfema.ampclient.fulltextsearch.SearchResult;
import com.anfema.ampclient.mediafiles.AmpFiles;
import com.anfema.ampclient.mediafiles.AmpFilesFactory;
import com.anfema.ampclient.mediafiles.AmpPicasso;
import com.anfema.ampclient.mediafiles.AmpPicassoFactory;
import com.anfema.ampclient.pages.AmpPages;
import com.anfema.ampclient.pages.AmpPagesFactory;
import com.anfema.ampclient.pages.models.Collection;
import com.anfema.ampclient.pages.models.Page;
import com.anfema.ampclient.pages.models.PagePreview;
import com.anfema.ampclient.utils.ContextUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;
import rx.Observable;
import rx.functions.Func1;

/**
 * Main entry point for AMP functionality. Obtain an instance with {@link #getInstance(AmpConfig, Context)}.
 * <p/>
 * Serving as entry point AmpClient holds interfaces providing the actual implementation of its functionality.
 */
public class AmpClient implements AmpPages, AmpFiles, AmpPicasso, AmpArchive, AmpFts
{
	/// Multiton

	private static Map<AmpConfig, AmpClient> instances = new HashMap<>();

	/**
	 * @param config configuration for AMP client
	 * @return client instance, ready to go
	 */
	public static AmpClient getInstance( AmpConfig config, Context context )
	{
		AmpConfig.assertConfigIsValid( config );

		// check if client for this configuration already exists, otherwise create an instance
		AmpClient storedClient = instances.get( config );
		if ( storedClient != null && storedClient.context != null )
		{
			return storedClient;
		}

		context = ContextUtils.getApplicationContext( context );
		AmpClient ampClient = new AmpClient( config, context );
		instances.put( config, ampClient );
		return ampClient;
	}

	/// Multiton END


	// stored to verify on #getInstance(AmpConfig, Context) that context (which is passed to delegate classes) is not null.
	private Context context;

	// delegate classes
	private final AmpPages   ampPages;
	private final AmpFiles   ampFiles;
	private final AmpPicasso ampPicasso;
	private final AmpArchive ampArchive;
	private final AmpFts     ampFts;

	private AmpClient( AmpConfig config, Context context )
	{
		this.context = context;
		ampPages = AmpPagesFactory.newInstance( config, context );
		ampFiles = AmpFilesFactory.newInstance( config, context );
		ampPicasso = AmpPicassoFactory.newInstance( ampFiles, config, context );
		ampArchive = AmpArchiveFactory.newInstance( ampPages, ampFiles, config, context );
		ampFts = AmpFtsFactory.newInstance( ampPages, ampFiles, config, context );
	}


	/// Collection and page calls

	/**
	 * Call collections on Amp API.
	 * Adds collection identifier and authorization token to request as retrieved via {@link AmpConfig}<br/>
	 */
	@Override
	public Observable<Collection> getCollection()
	{
		return ampPages.getCollection();
	}

	/**
	 * Add collection identifier and authorization token to request.<br/>
	 */
	@Override
	public Observable<Page> getPage( String pageIdentifier )
	{
		return ampPages.getPage( pageIdentifier );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.<br/>
	 */
	@Override
	public Observable<Page> getAllPages()
	{
		return ampPages.getAllPages();
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.<br/>
	 */
	@Override
	public Observable<Page> getPages( Func1<PagePreview, Boolean> pagesFilter )
	{
		return ampPages.getPages( pagesFilter );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.
	 * <p/>
	 * The pages are ordered by their position.
	 */
	@Override
	public Observable<Page> getPagesSorted( Func1<PagePreview, Boolean> pagesFilter )
	{
		return ampPages.getPagesSorted( pagesFilter );
	}

	/**
	 * A set of page previews is "returned" by emitting multiple events.
	 * <p/>
	 * The page previews are ordered by their position.
	 */
	@Override
	public Observable<PagePreview> getPagePreviewsSorted( Func1<PagePreview, Boolean> pagesFilter )
	{
		return ampPages.getPagePreviewsSorted( pagesFilter );
	}

	// Loading media files

	/**
	 * @see AmpFiles#request(HttpUrl, String)
	 */
	@Override
	public Observable<File> request( HttpUrl url, String checksum )
	{
		return ampFiles.request( url, checksum );
	}

	/**
	 * @see AmpFiles#request(HttpUrl, String, boolean, File)
	 */
	@Override
	public Observable<File> request( HttpUrl url, String checksum, boolean ignoreCaching, File targetFile )
	{
		return ampFiles.request( url, checksum, ignoreCaching, targetFile );
	}

	@Override
	public RequestCreator loadImage( String path )
	{
		return ampPicasso.loadImage( path );
	}

	@Override
	public RequestCreator loadImage( Uri uri )
	{
		return ampPicasso.loadImage( uri );
	}

	@Override
	public RequestCreator loadImage( int resourceID )
	{
		return ampPicasso.loadImage( resourceID );
	}

	@Override
	public Picasso getPicassoInstance()
	{
		return ampPicasso.getPicassoInstance();
	}


	/// Archive download

	/**
	 * @see AmpArchive#downloadArchive()
	 */
	@Override
	public Observable<File> downloadArchive()
	{
		return ampArchive.downloadArchive();
	}


	/// Full text search

	/**
	 * @see AmpFts#downloadSearchDatabase()
	 */
	@Override
	public Observable<File> downloadSearchDatabase()
	{
		return ampFts.downloadSearchDatabase();
	}

	/**
	 * @see AmpFts#fullTextSearch(String, String, String)
	 */
	@Override
	public Observable<List<SearchResult>> fullTextSearch( String searchTerm, String locale, String pageLayout )
	{
		return ampFts.fullTextSearch( searchTerm, locale, pageLayout );
	}
}
