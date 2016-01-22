package com.anfema.ampclient.archive;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.AmpFiles;
import com.anfema.ampclient.caching.FilePaths;
import com.anfema.ampclient.caching.MemoryCache;
import com.anfema.ampclient.pages.AmpPages;
import com.anfema.ampclient.pages.AmpPagesWithCaching;
import com.anfema.ampclient.pages.CollectionDownloadedListener;
import com.anfema.ampclient.pages.models.Collection;
import com.anfema.ampclient.utils.Log;
import com.anfema.ampclient.utils.RxUtils;

import java.io.File;

import okhttp3.HttpUrl;
import rx.Observable;

class AmpArchiveDownloader implements AmpArchive, CollectionDownloadedListener
{
	private final AmpPages    ampPages;
	private final AmpConfig   config;
	private final Context     context;
	private final MemoryCache memoryCache;

	public AmpArchiveDownloader( AmpPages ampPages, AmpConfig config, Context context )
	{
		this.ampPages = ampPages;
		this.config = config;
		this.context = context;

		// if ampPages uses caching provide archive update check when collection downloaded
		if ( ampPages instanceof AmpPagesWithCaching )
		{
			AmpPagesWithCaching ampPagesWithCaching = ( AmpPagesWithCaching ) ampPages;
			ampPagesWithCaching.setCollectionListener( this );
			memoryCache = ampPagesWithCaching.getMemoryCache();
		}
		else
		{
			memoryCache = null;
		}
	}

	/**
	 * Prevent multiple archive downloads at the same time.
	 */
	boolean activeArchiveDownload = false;

	/**
	 * Download the archive file for current collection, which should make app usable in offline mode.
	 */
	@Override
	public Observable<File> downloadArchive()
	{
		return downloadArchive( null, null );
	}

	/**
	 * Download the archive file for current collection, which should make app usable in offline mode.
	 *
	 * @param inCollection If collection already is available it can be passed in order to save time.
	 * @param lastModified
	 */
	public Observable<File> downloadArchive( Collection inCollection, String lastModified )
	{
		if ( inCollection != null && !inCollection.identifier.equals( config.collectionIdentifier ) )
		{
			Exception e = new Exception( "Archive download: inCollection.identifier: " + inCollection.identifier + " does not match config's collectionIdentifier: " + config.collectionIdentifier );
			Log.ex( e );
			return Observable.error( e );
		}

		File archivePath = FilePaths.getArchiveFilePath( config.collectionIdentifier, context );
		Log.i( "AMP Archive", "about to download archive for collection " + config.collectionIdentifier );

		activeArchiveDownload = true;

		// use inCollection or retrieve by making a collections call
		Observable<Collection> collectionObs;
		if ( inCollection == null )
		{
			collectionObs = ampPages.getCollection();
		}
		else
		{
			collectionObs = Observable.just( inCollection );
		}

		Observable<File> archiveObs = collectionObs
				.map( collection -> collection.archive )
				.flatMap( archiveUrl -> AmpFiles.getInstance( config.authorizationHeaderValue, context ).request( HttpUrl.parse( archiveUrl ), archivePath ) );

		return RxUtils.flatCombineLatest( collectionObs, archiveObs, ( collection, archivePath2 ) -> ArchiveUtils.unTar( archivePath2, collection, lastModified, config, memoryCache, context ) )
				.doOnNext( file -> activeArchiveDownload = false )
				.compose( RxUtils.runOnIoThread() );
	}

	/**
	 * Check if archive needs to be updated.
	 */
	@Override
	public void collectionDownloaded( Collection collection, String lastModified )
	{
		if ( config.archiveDownloads && !activeArchiveDownload )
		{
			// archive needs to be downloaded again. Download runs in background and does not even inform UI when finished
			downloadArchive( collection, lastModified )
					.subscribe( file -> Log.d( "AMP Archive", "Archive has been downloaded/updated in background" ) );
		}
	}
}