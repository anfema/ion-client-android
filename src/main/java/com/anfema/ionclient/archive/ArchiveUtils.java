package com.anfema.ionclient.archive;

import android.content.Context;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.archive.models.ArchiveIndex;
import com.anfema.ionclient.caching.FilePaths;
import com.anfema.ionclient.caching.MemoryCache;
import com.anfema.ionclient.caching.index.CacheIndexStore;
import com.anfema.ionclient.caching.index.CollectionCacheIndex;
import com.anfema.ionclient.caching.index.FileCacheIndex;
import com.anfema.ionclient.caching.index.PageCacheIndex;
import com.anfema.ionclient.exceptions.NoIonPagesRequestException;
import com.anfema.ionclient.exceptions.PageNotInCollectionException;
import com.anfema.ionclient.pages.IonPageUrls;
import com.anfema.ionclient.pages.IonPageUrls.IonRequestInfo;
import com.anfema.ionclient.pages.IonPageUrls.IonRequestType;
import com.anfema.ionclient.pages.models.Collection;
import com.anfema.ionclient.pages.models.responses.CollectionResponse;
import com.anfema.ionclient.serialization.GsonHolder;
import com.anfema.ionclient.utils.FileUtils;
import com.anfema.ionclient.utils.IonLog;

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

	static Observable<File> unTar( File archiveFile, Collection collection, String lastModified, IonConfig config, Context context )
	{
		return Observable.just( null )
				.flatMap( o -> {
					try
					{
						return Observable.from( performUnTar( archiveFile, config, collection, lastModified, context ) )
								// write cache index entries
								.doOnNext( fileWithType -> saveCacheIndex( fileWithType, collection, lastModified, config, context ) )
								.map( fileWithType -> fileWithType.file );
					}
					catch ( IOException | ArchiveException e )
					{
						return Observable.error( e );
					}
				} );
	}

	/**
	 * Untar an input file into an output file.
	 * <p>
	 * The output file is created in the output folder, having the same name
	 * as the input file, minus the '.tar' extension.
	 *
	 * @param archiveFile  input TAR file
	 * @param lastModified when collection has been last modified
	 * @throws FileNotFoundException
	 * @throws ArchiveException
	 */
	private static List<FileWithMeta> performUnTar( File archiveFile, IonConfig config, Collection collection, String lastModified, Context context ) throws FileNotFoundException, IOException, ArchiveException
	{
		File collectionFolder = FilePaths.getCollectionFolderPath( config, context );
		File collectionFolderTemp = FilePaths.getTempFilePath( collectionFolder );

		final List<FileWithMeta> untaredFiles = new LinkedList<>();

		InputStream is = null;
		TarArchiveInputStream debInputStream = null;
		try
		{
			IonLog.d( TAG, String.format( "Untaring %s to dir %s.", archiveFile.getPath(), collectionFolder.getPath() ) );

			is = new FileInputStream( archiveFile );
			debInputStream = ( TarArchiveInputStream ) new ArchiveStreamFactory().createArchiveInputStream( "tar", is );

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
						IonLog.w( TAG, "Skipping " + entry.getName() + " because it was not found in index.json." );
						continue;
					}

					IonLog.i( TAG, fileInfo.url );
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
		}
		finally
		{
			// finished reading TAR archive
			if ( is != null )
			{
				is.close();
			}
			if ( debInputStream != null )
			{
				debInputStream.close();
			}
			if ( archiveFile != null && archiveFile.exists() )
			{
				archiveFile.delete();
			}
		}

		// if lastModified date was not passed, look if cache index entry exists for collection and retrieve it from there
		if ( collection != null && lastModified == null )
		{
			CollectionCacheIndex collectionCacheIndex = CollectionCacheIndex.retrieve( config, context );
			lastModified = collectionCacheIndex == null ? null : collectionCacheIndex.getLastModified();
			IonLog.d( TAG, "Restoring last_modified from cache index: " + lastModified );
		}

		// delete old cache index entries of the collection in shared preferences and in memory cache
		CacheIndexStore.clearCollection( config, context );
		MemoryCache.clear();

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
		else
		{
			IonLog.w( TAG, "No media files were contained in archive." );
		}

		// add collection to file cache again
		if ( collection != null )
		{
			MemoryCache.saveCollection( collection, config );
			try
			{
				saveCollectionToFileCache( config, collection, context );
				CollectionCacheIndex.save( config, context, lastModified );
			}
			catch ( IOException e )
			{
				IonLog.e( "ION Archive", "Collection could not be saved." );
				IonLog.ex( e );
			}
		}

		// cache index entries are not written yet at this point
		return untaredFiles;
	}

	private static void saveCollectionToFileCache( IonConfig config, Collection collection, Context context ) throws IOException
	{
		String collectionUrl = IonPageUrls.getCollectionUrl( config );
		File filePath = FilePaths.getCollectionJsonPath( collectionUrl, config, context );
		String collectionJson = GsonHolder.getInstance().toJson( new CollectionResponse( collection ) );
		FileUtils.writeTextToFile( collectionJson, filePath );
	}

	private static FileWithMeta getFilePath( ArchiveIndex fileInfo, File collectionFolderTemp, IonConfig config, Context context )
	{
		File targetFile;
		IonRequestType type = null;
		String url = fileInfo.url;
		String pageIdentifier = null;
		String filename = FilePaths.getFileName( url );
		try
		{
			// check URL is a collections or pages call
			IonRequestInfo requestInfo = IonPageUrls.analyze( url, config );
			pageIdentifier = requestInfo.pageIdentifier;
			type = requestInfo.requestType;
			targetFile = FilePaths.getFilePath( url, config, context, true );
		}

		catch ( NoIonPagesRequestException e )
		{
			IonLog.w( TAG, "URL " + url + " cannot be handled properly. Is it invalid?" );
			targetFile = new File( collectionFolderTemp, filename );
		}
		return new FileWithMeta( targetFile, type, fileInfo, pageIdentifier );
	}

	public static class FileWithMeta
	{
		File           file;
		IonRequestType type;
		ArchiveIndex   archiveIndex;
		String         pageIdentifier;

		public FileWithMeta( File file, IonRequestType type, ArchiveIndex archiveIndex, String pageIdentifier )
		{
			this.file = file;
			this.type = type;
			this.archiveIndex = archiveIndex;
			this.pageIdentifier = pageIdentifier;
		}
	}

	private static void saveCacheIndex( FileWithMeta fileWithMeta, Collection collection, String lastModified, IonConfig config, Context context )
	{
		IonRequestType type = fileWithMeta.type;
		if ( type == null )
		{
			IonLog.w( TAG, "It could not be determined of which kind the request " + fileWithMeta.archiveIndex.url + " is. Thus, do not create a cache index entry." );
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
					IonLog.ex( TAG, e );
				}
				PageCacheIndex.save( pageIdentifier, lastChanged, config, context );
				break;
			case MEDIA:
				FileCacheIndex.save( fileWithMeta.archiveIndex.url, fileWithMeta.file, config, fileWithMeta.archiveIndex.checksum, context );
				break;
		}
	}
}
