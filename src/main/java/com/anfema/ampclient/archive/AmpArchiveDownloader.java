package com.anfema.ampclient.archive;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.AmpFiles;
import com.anfema.ampclient.caching.CollectionCacheIndex;
import com.anfema.ampclient.caching.FilePaths;
import com.anfema.ampclient.pages.AmpPages;
import com.anfema.ampclient.pages.AmpPagesWithCaching;
import com.anfema.ampclient.pages.CollectionDownloadedListener;
import com.anfema.ampclient.pages.models.Collection;
import com.anfema.ampclient.utils.Log;
import com.anfema.ampclient.utils.RxUtils;
import com.squareup.okhttp.HttpUrl;

import java.io.File;

import rx.Observable;

class AmpArchiveDownloader implements AmpArchive, CollectionDownloadedListener
{
	private final AmpPages  ampPages;
	private final AmpConfig config;
	private final Context   context;

	public AmpArchiveDownloader( AmpPages ampPages, AmpConfig config, Context context )
	{
		this.ampPages = ampPages;
		this.config = config;
		this.context = context;

		// if ampPages uses caching provide archive update check when collection downloaded
		if ( ampPages instanceof AmpPagesWithCaching )
		{
			( ( AmpPagesWithCaching ) ampPages ).setCollectionListener( this );
		}
	}

	/**
	 * Download the archive file for current collection, which should make app usable in offline mode.
	 */
	@Override
	public Observable<File> downloadArchive()
	{
		File archivePath = FilePaths.getArchiveFilePath( config.collectionIdentifier, context );
		Log.i( "AMP Archive", "about to download archive for collection " + config.collectionIdentifier );

		Observable<Collection> collectionObs = ampPages.getCollection();

		Observable<File> archiveObs = collectionObs
				.map( collection -> collection.archive )
				.flatMap( archiveUrl -> AmpFiles.getInstance( config.authorizationHeaderValue, context ).request( HttpUrl.parse( archiveUrl ), archivePath ) );

		return RxUtils.flatCombineLatest( collectionObs, archiveObs, ( collection, archivePath2 ) -> ArchiveUtils.unTar( archivePath2, collection, config, context ) )
				.compose( RxUtils.runOnIoThread() );
	}

	/**
	 * check if archive needs to be updated
	 *
	 * @param collection
	 * @param oldCacheIndex
	 */
	@Override
	public void collectionDownloaded( Collection collection, CollectionCacheIndex oldCacheIndex )
	{
		// have pages changed in collection?
		if ( oldCacheIndex == null || collection.getLastChanged().isAfter( oldCacheIndex.getLastChanged() ) )
		{
			// archive needs to be downloaded again. Download runs in background and does not even inform UI when finished
			downloadArchive()
					.subscribe();
		}
	}
}