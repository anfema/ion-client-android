package com.anfema.ampclient.archive;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.AmpFiles;
import com.anfema.ampclient.caching.FilePaths;
import com.anfema.ampclient.pages.AmpPages;
import com.anfema.ampclient.pages.models.Collection;
import com.anfema.ampclient.utils.Log;
import com.anfema.ampclient.utils.RxUtils;
import com.squareup.okhttp.HttpUrl;

import java.io.File;

import rx.Observable;

class AmpArchiveDownloader implements AmpArchive
{
	private final AmpPages  ampPages;
	private final AmpConfig config;
	private final Context   context;

	public AmpArchiveDownloader( AmpPages ampPages, AmpConfig config, Context context )
	{
		this.ampPages = ampPages;
		this.config = config;
		this.context = context;
	}

	/**
	 * Download the archive file for current collection, which should make app usable in offline mode.
	 */
	@Override
	public Observable<File> downloadArchive()
	{
		File archivePath = FilePaths.getArchiveFilePath( config.collectionIdentifier, context );
		Log.i( "FTS Database", "about to download FTS database for collection " + config.collectionIdentifier );

		Observable<Collection> collectionObs = ampPages.getCollection();

		Observable<File> archiveObs = collectionObs
				.map( collection -> collection.archive )
				.flatMap( archiveUrl -> AmpFiles.getInstance( config.authorizationHeaderValue, context ).request( HttpUrl.parse( archiveUrl ), archivePath ) );

		return RxUtils.flatCombineLatest( collectionObs, archiveObs, ( collection, archivePath2 ) -> ArchiveUtils.unTar( archivePath2, collection, config, context ) );
	}
}