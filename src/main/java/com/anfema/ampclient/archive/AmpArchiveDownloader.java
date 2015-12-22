package com.anfema.ampclient.archive;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.AmpFiles;
import com.anfema.ampclient.caching.FilePaths;
import com.anfema.ampclient.pages.AmpPages;
import com.anfema.ampclient.utils.Log;
import com.squareup.okhttp.HttpUrl;

import java.io.File;

import rx.Observable;

public class AmpArchiveDownloader implements AmpArchive
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
		return ampPages.getCollection()
				.map( collection -> collection.archive )
				.flatMap( zipUrl -> AmpFiles.getInstance( config.authorizationHeaderValue, context ).request( HttpUrl.parse( zipUrl ), archivePath ) )
				.flatMap( archive -> ArchiveUtils.unTar( archivePath, config, context ) );
	}
}