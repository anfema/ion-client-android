package com.anfema.ionclient;

import android.content.Context;

import com.anfema.ionclient.archive.IonArchive;
import com.anfema.ionclient.archive.IonArchiveDownloader;
import com.anfema.ionclient.mediafiles.FileWithStatus;
import com.anfema.ionclient.mediafiles.IonFiles;
import com.anfema.ionclient.mediafiles.IonFilesWithCaching;
import com.anfema.ionclient.pages.IonPages;
import com.anfema.ionclient.pages.IonPagesFactory;
import com.anfema.ionclient.pages.models.Collection;
import com.anfema.ionclient.pages.models.Page;
import com.anfema.ionclient.pages.models.PagePreview;
import com.anfema.ionclient.pages.models.contents.Downloadable;
import com.anfema.ionclient.utils.ContextUtils;
import com.anfema.ionclient.utils.IonLog;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import okhttp3.HttpUrl;

/**
 * Main entry point for ION functionality. Obtain an instance with {@link #getInstance(IonConfig, Context)}.
 * <p>
 * Serving as entry point IonClient holds interfaces providing the actual implementation of its functionality.
 */
public class IonClient implements IonPages, IonFiles, IonArchive
{
	/// Multiton

	private static final Map<IonConfig, IonClient> instances = new HashMap<>();

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
	private final Context context;

	// delegate classes
	private final IonPages   ionPages;
	private final IonFiles   ionFiles;
	private final IonArchive ionArchive;

	private IonClient( IonConfig config, Context context )
	{
		this.context = context;
		ionPages = IonPagesFactory.newInstance( config, context );
		ionFiles = new IonFilesWithCaching( config, context );
		ionArchive = new IonArchiveDownloader( ionPages, ionFiles, config, context );
	}

	@Override
	public void updateConfig( IonConfig config )
	{
		ionPages.updateConfig( config );
		ionFiles.updateConfig( config );
		ionArchive.updateConfig( config );
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
	public Single<Collection> fetchCollection( boolean preferNetwork )
	{
		return ionPages.fetchCollection( preferNetwork );
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

	@Override
	public Single<FileWithStatus> request( Downloadable content )
	{
		return ionFiles.request( content );
	}

	@Override
	public Single<FileWithStatus> request( HttpUrl url, String checksum )
	{
		return ionFiles.request( url, checksum );
	}

	@Override
	public Single<FileWithStatus> request( HttpUrl url, String checksum, boolean ignoreCaching, @Nullable File targetFile )
	{
		return ionFiles.request( url, checksum, ignoreCaching, targetFile );
	}

	@Override
	public Single<FileWithStatus> request( HttpUrl url, HttpUrl downloadUrl, String checksum, boolean ignoreCaching, @Nullable File targetFile )
	{
		return ionFiles.request( url, downloadUrl, checksum, ignoreCaching, targetFile );
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
}
