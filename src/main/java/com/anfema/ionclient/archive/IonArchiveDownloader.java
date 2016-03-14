package com.anfema.ionclient.archive;

import android.content.Context;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.caching.FilePaths;
import com.anfema.ionclient.mediafiles.IonFiles;
import com.anfema.ionclient.pages.CollectionDownloadedListener;
import com.anfema.ionclient.pages.IonPages;
import com.anfema.ionclient.pages.IonPagesWithCaching;
import com.anfema.ionclient.pages.models.Collection;
import com.anfema.ionclient.utils.Log;
import com.anfema.ionclient.utils.RxUtils;

import java.io.File;

import okhttp3.HttpUrl;
import rx.Observable;

class IonArchiveDownloader implements IonArchive, CollectionDownloadedListener
{
	private final IonPages  ionPages;
	private final IonFiles  ionFiles;
	private       IonConfig config;
	private final Context   context;

	public IonArchiveDownloader( IonPages ionPages, IonFiles ionFiles, IonConfig config, Context context )
	{
		this.ionPages = ionPages;
		this.ionFiles = ionFiles;
		this.config = config;
		this.context = context;

		// if ionPages uses caching provide archive update check when collection downloaded
		if ( ionPages instanceof IonPagesWithCaching )
		{
			IonPagesWithCaching ionPagesWithCaching = ( IonPagesWithCaching ) ionPages;
			ionPagesWithCaching.setCollectionListener( this );
		}
	}

	@Override
	public void updateConfig( IonConfig config )
	{
		this.config = config;
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
	 * @param lastModified when the collection has been last modified
	 */
	public Observable<File> downloadArchive( Collection inCollection, String lastModified )
	{
		if ( inCollection != null && !inCollection.identifier.equals( config.collectionIdentifier ) )
		{
			Exception e = new Exception( "Archive download: inCollection.identifier: " + inCollection.identifier + " does not match config's collectionIdentifier: " + config.collectionIdentifier );
			Log.ex( e );
			return Observable.error( e );
		}

		File archivePath = FilePaths.getArchiveFilePath( config, context );
		Log.i( "ION Archive", "about to download archive for collection " + config.collectionIdentifier );

		activeArchiveDownload = true;

		// use inCollection or retrieve by making a collections call
		Observable<Collection> collectionObs;
		if ( inCollection == null )
		{
			collectionObs = ionPages.fetchCollection();
		}
		else
		{
			collectionObs = Observable.just( inCollection );
		}

		Observable<File> archiveObs = collectionObs
				.map( collection -> collection.archive )
				.flatMap( archiveUrl -> ionFiles.request( HttpUrl.parse( archiveUrl ), null, true, archivePath ) );

		return RxUtils.flatCombineLatest( collectionObs, archiveObs, ( collection, archivePath2 ) -> ArchiveUtils.unTar( archivePath2, collection, lastModified, config, context ) )
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
					.subscribe( RxUtils.NOTHING, RxUtils.DEFAULT_EXCEPTION_HANDLER, () -> Log.d( "ION Archive", "Archive has been downloaded/updated in background" ) );
		}
	}
}