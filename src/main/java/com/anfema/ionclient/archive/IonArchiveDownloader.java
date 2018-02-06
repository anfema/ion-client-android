package com.anfema.ionclient.archive;

import android.content.Context;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.caching.FilePaths;
import com.anfema.ionclient.mediafiles.IonFiles;
import com.anfema.ionclient.pages.CollectionDownloadedListener;
import com.anfema.ionclient.pages.IonPages;
import com.anfema.ionclient.pages.IonPagesWithCaching;
import com.anfema.ionclient.pages.models.Collection;
import com.anfema.ionclient.utils.IonLog;
import com.anfema.ionclient.utils.RxUtils;

import java.io.File;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;

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
	public Completable downloadArchive()
	{
		return downloadArchive( null, null );
	}

	/**
	 * Download the archive file for current collection, which should make app usable in offline mode.
	 *
	 * @param inCollection If collection already is available it can be passed in order to save time.
	 * @param lastModified when the collection has been last modified
	 */
	public Completable downloadArchive( Collection inCollection, String lastModified )
	{
		if ( inCollection != null && !inCollection.identifier.equals( config.collectionIdentifier ) )
		{
			Exception e = new Exception( "Archive download: inCollection.identifier: " + inCollection.identifier + " does not match config's collectionIdentifier: " + config.collectionIdentifier );
			IonLog.ex( e );
			return Completable.error( e );
		}

		File archivePath = FilePaths.getArchiveFilePath( config, context );
		IonLog.i( "ION Archive", "about to download archive for collection " + config.collectionIdentifier );

		activeArchiveDownload = true;

		// use inCollection or retrieve by making a collections call
		Single<Collection> collectionObs;
		if ( inCollection == null )
		{
			collectionObs = ionPages.fetchCollection();
		}
		else
		{
			collectionObs = Single.just( inCollection );
		}

		return collectionObs.flatMap( collection ->
				// download archive
				ionFiles.request( HttpUrl.parse( collection.archive ), null, false, archivePath )
						.map( fileWithStatus -> fileWithStatus.file )
						.map( archiveFile -> new CollectionArchive( collection, archiveFile ) ) )
				// untar archive
				.flatMapObservable( collArch -> ArchiveUtils.unTar( collArch.archivePath, collArch.collection, lastModified, config, context ) )
				.ignoreElements()
				.doFinally( () -> activeArchiveDownload = false )
				.subscribeOn( Schedulers.io() );
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
					.subscribe( () -> IonLog.d( "ION Archive", "Archive has been downloaded/updated in background" ), RxUtils.DEFAULT_EXCEPTION_HANDLER );
		}
	}

	class CollectionArchive
	{
		Collection collection;
		File       archivePath;

		CollectionArchive( Collection collection, File archivePath )
		{
			this.collection = collection;
			this.archivePath = archivePath;
		}
	}
}