package com.anfema.ampclient.archive;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.caching.CacheIndexStore;
import com.anfema.ampclient.caching.CollectionCacheIndex;
import com.anfema.ampclient.caching.FileCacheIndex;
import com.anfema.ampclient.caching.FilePaths;
import com.anfema.ampclient.caching.MemoryCache;
import com.anfema.ampclient.caching.PageCacheIndex;
import com.anfema.ampclient.exceptions.NoAmpPagesRequestException;
import com.anfema.ampclient.exceptions.PageNotInCollectionException;
import com.anfema.ampclient.pages.AmpRequest;
import com.anfema.ampclient.pages.AmpRequest.AmpRequestInfo;
import com.anfema.ampclient.pages.PagesUrls;
import com.anfema.ampclient.pages.models.Collection;
import com.anfema.ampclient.pages.models.responses.CollectionResponse;
import com.anfema.ampclient.serialization.GsonHolder;
import com.anfema.ampclient.utils.FileUtils;
import com.anfema.ampclient.utils.Log;
import com.anfema.ampclient.utils.RxUtils;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import rx.Observable;

class ArchiveUtils
{
	private static final String TAG = "ArchiveUtils";

	static Observable<File> unTar( File archiveFile, Collection collection, String lastModified, AmpConfig config, MemoryCache memoryCache, Context context )
	{
		return Observable.just( null )
				.flatMap( o -> {
					try
					{
						return Observable.from( performUnTar( archiveFile, config, collection, lastModified, memoryCache, context ) )
								// write cache index entries
								.doOnNext( fileWithType -> saveCacheIndex( fileWithType, collection, lastModified, config, context ) )
								.map( fileWithType -> fileWithType.file );
					}
					catch ( IOException | ArchiveException e )
					{
						return Observable.error( e );
					}
				} )
				.compose( RxUtils.runOnComputionThread() );
	}

	/**
	 * Untar an input file into an output file.
	 * <p/>
	 * The output file is created in the output folder, having the same name
	 * as the input file, minus the '.tar' extension.
	 *
	 * @param archiveFile
	 * @param config
	 * @param collection
	 * @param lastModified
	 * @param memoryCache  @return The {@link List} of {@link File}s with the untared content.  @throws IOException
	 * @throws FileNotFoundException
	 * @throws ArchiveException
	 */
	private static List<FileWithMeta> performUnTar( File archiveFile, AmpConfig config, Collection collection, String lastModified, MemoryCache memoryCache, Context context ) throws FileNotFoundException, IOException, ArchiveException
	{
		File collectionFolder = FilePaths.getCollectionFolderPath( config, context );

		Log.d( TAG, String.format( "Untaring %s to dir %s.", archiveFile.getPath(), collectionFolder.getPath() ) );

		File collectionFolderTemp = FilePaths.getTempFilePath( collectionFolder );

		final List<FileWithMeta> untaredFiles = new LinkedList<>();
		final InputStream is = new FileInputStream( archiveFile );
		final TarArchiveInputStream debInputStream = ( TarArchiveInputStream ) new ArchiveStreamFactory().createArchiveInputStream( "tar", is );

		TarArchiveEntry entry;
		List<ArchiveIndex> index = null;
		boolean indexHasBeenRead = false;
		while ( ( entry = ( TarArchiveEntry ) debInputStream.getNextEntry() ) != null )
		{
			if ( !indexHasBeenRead )
			{
				// get index.json
				InputStreamReader inputStreamReader = new InputStreamReader( debInputStream, "UTF-8" );
				index = Arrays.asList( GsonHolder.getInstance().fromJson( inputStreamReader, ArchiveIndex[].class ) );
				indexHasBeenRead = true;
				continue;
			}

			// write the "content" files
			if ( !entry.isDirectory() )
			{
				String archiveFileName = entry.getName();
				ArchiveIndex fileInfo = ArchiveIndex.getByName( archiveFileName, index );
				if ( fileInfo == null )
				{
					Log.w( TAG, "Skipping " + entry.getName() + " because it was not found in index.json." );
					continue;
				}

				FileWithMeta fileWithMeta = getFilePath( fileInfo, collectionFolderTemp, config, context );
				File targetFile = fileWithMeta.file;
				FileUtils.createDir( targetFile.getParentFile() );

				targetFile = FileUtils.writeToFile( debInputStream, targetFile );

				if ( targetFile != null )
				{
					untaredFiles.add( fileWithMeta );
				}
			}
		}

		// finished reading TAR archive
		debInputStream.close();
		archiveFile.delete();

		// if lastModified date was not passed, look if cache index entry exists for collection and retrieve it from there
		if ( collection != null && lastModified == null )
		{
			CollectionCacheIndex collectionCacheIndex = CollectionCacheIndex.retrieve( PagesUrls.getCollectionUrl( config ), config.collectionIdentifier, context );
			lastModified = collectionCacheIndex == null ? null : collectionCacheIndex.getLastModified();
			Log.d( TAG, "Restoring last_modified from cache index: " + lastModified );
		}

		// delete old cache index entries of the collection in shared preferences and in memory cache
		CacheIndexStore.clear( config.collectionIdentifier, context );
		memoryCache.clearPagesMemCache();

		// replace collection folder (containing json files) - deletes old file cache
		boolean jsonWriteSuccess = FileUtils.move( collectionFolderTemp, collectionFolder, true );
		if ( !jsonWriteSuccess )
		{
			throw new IOException( "JSON files could not be moved to final path." );
		}

		// replace media files in collection
		File mediaFolderTemp = FilePaths.getMediaFolderPath( config, context, true );
		File mediaFolder = FilePaths.getMediaFolderPath( config, context, false );
		if ( mediaFolderTemp.exists() )
		{
			boolean mediaWriteSuccess = FileUtils.move( mediaFolderTemp, mediaFolder, true );
			if ( !mediaWriteSuccess )
			{
				throw new IOException( "Media files could not be moved to final path." );
			}
		}

		// add collection to file cache again
		if ( collection != null )
		{
			memoryCache.setCollection( collection );
			try
			{
				saveCollectionToFileCache( config, collection, context );
				CollectionCacheIndex.save( config, context, lastModified );
			}
			catch ( NoAmpPagesRequestException | IOException e )
			{
				Log.e( "AMP Archive", "Collection could not be saved." );
				Log.ex( e );
			}
		}

		// cache index entries are not written yet at this point
		return untaredFiles;
	}

	private static void saveCollectionToFileCache( AmpConfig config, Collection collection, Context context ) throws NoAmpPagesRequestException, IOException
	{
		String collectionUrl = PagesUrls.getCollectionUrl( config );
		File filePath = FilePaths.getJsonFilePath( collectionUrl, config, context );
		String collectionJson = GsonHolder.getInstance().toJson( new CollectionResponse( collection ) );
		FileUtils.writeTextToFile( collectionJson, filePath );
	}

	private static FileWithMeta getFilePath( ArchiveIndex fileInfo, File collectionFolderTemp, AmpConfig config, Context context )
	{
		File targetFile;
		AmpRequest type = null;
		String url = fileInfo.url;
		String pageIdentifier = null;
		String filename = FilePaths.getFileName( url );
		try
		{
			// check URL is a collections or pages call
			AmpRequestInfo requestInfo = AmpRequest.analyze( url, config );
			pageIdentifier = requestInfo.pageIdentifier;
			type = requestInfo.requestType;
			targetFile = FilePaths.getFilePath( url, config, context, true );
		}

		catch ( NoAmpPagesRequestException e )
		{
			Log.w( TAG, "URL " + url + " cannot be handled properly. Is it invalid?" );
			targetFile = new File( collectionFolderTemp, filename );
		}
		return new FileWithMeta( targetFile, type, fileInfo, pageIdentifier );
	}

	public static class FileWithMeta
	{
		File         file;
		AmpRequest   type;
		ArchiveIndex archiveIndex;
		String       pageIdentifier;

		public FileWithMeta( File file, AmpRequest type, ArchiveIndex archiveIndex, String pageIdentifier )
		{
			this.file = file;
			this.type = type;
			this.archiveIndex = archiveIndex;
			this.pageIdentifier = pageIdentifier;
		}
	}

	private static void saveCacheIndex( FileWithMeta fileWithMeta, Collection collection, String lastModified, AmpConfig config, Context context )
	{
		AmpRequest type = fileWithMeta.type;
		if ( type == null )
		{
			Log.w( TAG, "It could not be determined of which kind the request " + fileWithMeta.archiveIndex.url + " is. Thus, do not create a cache index entry." );
			return;
		}

		switch ( type )
		{

		case COLLECTION:
			CollectionCacheIndex.save( config, context, lastModified );
			break;
		case PAGE:
			String pageIdentifier = fileWithMeta.pageIdentifier;
			DateTime lastChanged = null;
			try
			{
				lastChanged = collection.getPageLastChanged( pageIdentifier );
			}
			catch ( PageNotInCollectionException e )
			{
				Log.ex( TAG, e );
			}
			PageCacheIndex.save( pageIdentifier, lastChanged, config, context );
			break;
		case MEDIA:
			FileCacheIndex.save( fileWithMeta.archiveIndex.url, fileWithMeta.file, config, fileWithMeta.archiveIndex.checksum, context );
			break;
		}
	}
}
