package com.anfema.ampclient.archive;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.caching.CacheIndexStore;
import com.anfema.ampclient.caching.CollectionCacheIndex;
import com.anfema.ampclient.caching.FilePaths;
import com.anfema.ampclient.caching.MemoryCache;
import com.anfema.ampclient.caching.PageCacheIndex;
import com.anfema.ampclient.exceptions.NoAmpPagesRequestException;
import com.anfema.ampclient.exceptions.PageNotInCollectionException;
import com.anfema.ampclient.pages.AmpCallType;
import com.anfema.ampclient.pages.PagesUrls;
import com.anfema.ampclient.pages.models.Collection;
import com.anfema.ampclient.serialization.GsonHolder;
import com.anfema.ampclient.utils.FileUtils;
import com.anfema.ampclient.utils.Log;
import com.anfema.ampclient.utils.RxUtils;
import com.anfema.ampclient.utils.StringUtils;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import okhttp3.HttpUrl;
import rx.Observable;

class ArchiveUtils
{
	private static final String TAG = "ArchiveUtils";

	static Observable<File> unTar( File archiveFile, Collection collection, AmpConfig config, MemoryCache memoryCache, Context context )
	{
		return Observable.just( null )
				.flatMap( o -> {
					try
					{
						return Observable.from( performUnTar( archiveFile, config, collection, memoryCache, context ) )
								// write cache index entries
								.doOnNext( fileWithType -> saveCacheIndex( fileWithType, collection, config, context ) )
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
	 * <p>
	 * The output file is created in the output folder, having the same name
	 * as the input file, minus the '.tar' extension.
	 *
	 * @param archiveFile
	 * @param config
	 * @param collection
	 * @param memoryCache @return The {@link List} of {@link File}s with the untared content.
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ArchiveException
	 */
	private static List<FileWithMeta> performUnTar( File archiveFile, AmpConfig config, Collection collection, MemoryCache memoryCache, Context context ) throws FileNotFoundException, IOException, ArchiveException
	{
		File collectionFolder = FilePaths.getCollectionFolderPath( config.collectionIdentifier, context );

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

				FileWithMeta fileWithMeta = getFilePath( fileInfo, collectionFolderTemp, context );
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

		// delete old cache index entries of the collection as well as the pages' memory cache
		CacheIndexStore.clear( config.collectionIdentifier, context );
		memoryCache.clearPagesMemCache();

		// replace collection folder (containing json files) - deletes old file cache
		boolean writeSuccess = FileUtils.move( collectionFolderTemp, collectionFolder, true );

		if ( !writeSuccess )
		{
			throw new IOException( "Files could not be moved to final path." );
		}

		// add collection to file cache again
		if ( collection != null )
		{
			memoryCache.setCollection( collection );
			try
			{
				saveCollectionToFileCache( config, collection, context );
				CollectionCacheIndex.save( config, context, collection.getLastChanged() );
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
		File filePath = FilePaths.getJsonFilePath( collectionUrl, context );
		String collectionJson = GsonHolder.getInstance().toJson( collection );
		FileUtils.writeTextToFile( collectionJson, filePath );
	}

	private static FileWithMeta getFilePath( ArchiveIndex fileInfo, File collectionFolderTemp, Context context )
	{
		File targetFile;
		AmpCallType type = null;
		String url = fileInfo.url;
		String pageIdentifier = null;
		try
		{
			// check URL is a collections or pages call
			HttpUrl httpUrl = HttpUrl.parse( url );
			type = AmpCallType.determineCall( httpUrl );

			// build json file path with collection temp folder
			String filename = FilePaths.getFileName( url );

			List<String> urlPathSegments = httpUrl.pathSegments();
			int endpointIndex = FilePaths.findEndpointPathSegment( urlPathSegments );
			if ( endpointIndex > -1 && urlPathSegments.size() > endpointIndex + 1 )
			{
				List<String> remainingPathSegments = new ArrayList<>();

				// In case of page path is extended with page identifier folder
				// In case of collection no path is not extended at all
				for ( int i = endpointIndex + 2; i < urlPathSegments.size(); i++ )
				{
					remainingPathSegments.add( urlPathSegments.get( i ) );
					if ( type == AmpCallType.PAGES )
					{
						pageIdentifier = urlPathSegments.get( i );
					}
				}
				String folderPath = collectionFolderTemp.getPath() + FileUtils.SLASH + StringUtils.concatStrings( remainingPathSegments, FileUtils.SLASH );
				targetFile = new File( folderPath, filename );
			}
			else
			{
				Log.w( TAG, "URL " + url + " cannot be handled properly. Is it invalid?" );
				targetFile = new File( collectionFolderTemp, filename );
			}
		}
		catch ( NoAmpPagesRequestException e )
		{
			// URL is a protected media path
			// media files are directly written to files directory
			targetFile = FilePaths.getMediaFilePath( url, context );
		}
		return new FileWithMeta( targetFile, type, fileInfo, pageIdentifier );
	}

	public static class FileWithMeta
	{
		File         file;
		AmpCallType  type;
		ArchiveIndex archiveIndex;
		String       pageIdentifier;

		public FileWithMeta( File file, AmpCallType type, ArchiveIndex archiveIndex, String pageIdentifier )
		{
			this.file = file;
			this.type = type;
			this.archiveIndex = archiveIndex;
			this.pageIdentifier = pageIdentifier;
		}
	}

	private static void saveCacheIndex( FileWithMeta fileWithMeta, Collection collection, AmpConfig config, Context context )
	{
		AmpCallType type = fileWithMeta.type;
		if ( type == null )
		{
			// media file
			// TODO file caching to be implemented
		}
		else if ( type == AmpCallType.COLLECTIONS )
		{
			CollectionCacheIndex.save( config, context, collection.getLastChanged() );
		}
		else if ( type == AmpCallType.PAGES )
		{
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
		}
	}
}
