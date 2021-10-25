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
import com.anfema.ionclient.exceptions.FileMoveException;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import io.reactivex.Observable;


class ArchiveUtils
{
	private static final String TAG = "ArchiveUtils";

	static Observable<File> unTar( @NonNull File archiveFile, @NonNull Collection collection, String lastModified, DateTime requestTime,
								   IonConfig config, Context context )
	{
		return Observable.fromCallable( () -> performUnTar( archiveFile, config, collection, lastModified, requestTime, context ) )
				.flatMap( Observable::fromIterable )
				// write cache index entries
				.map( fileWithType -> saveCacheIndex( fileWithType, collection, lastModified, requestTime, config, context ) );
	}

	/**
	 * Untar an input file into an output file.
	 * <p>
	 * The output file is created in the output folder, having the same name
	 * as the input file, minus the '.tar' extension.
	 *
	 * @param archiveFile  input TAR file
	 * @param lastModified when collection has been last modified
	 * @param requestTime  the time, when the archive download was initiated
	 * @throws FileNotFoundException
	 * @throws ArchiveException
	 */
	private static List<FileWithMeta> performUnTar( File archiveFile, IonConfig config, Collection collection, String lastModified,
													DateTime requestTime, Context context ) throws FileNotFoundException, IOException, ArchiveException
	{
		File collectionFolder = FilePaths.getCollectionFolderPath( config, context );

		final List<FileWithMeta> untaredFiles = new LinkedList<>();

		InputStream is = null;
		TarArchiveInputStream debInputStream = null;
		List<ArchiveIndex> index = null;
		try
		{
			IonLog.d( TAG, String.format( "Untaring %s to dir %s.", archiveFile.getPath(), collectionFolder.getPath() ) );

			is = new FileInputStream( archiveFile );
			debInputStream = ( TarArchiveInputStream ) new ArchiveStreamFactory().createArchiveInputStream( "tar", is );

			TarArchiveEntry entry;
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
					FileWithMeta fileWithMeta = getFilePath( fileInfo, collectionFolder, config, context );
					File targetFile = fileWithMeta.fileTemp;
					FileUtils.createDir( targetFile.getParentFile() );

					targetFile = FileUtils.writeToFile( debInputStream, targetFile );

					if ( targetFile != null )
					{
						untaredFiles.add( fileWithMeta );
					}
				}
			}
			IonLog.d( TAG, "Number of index entries found in archive: " + index.size() );
			IonLog.d( TAG, "Number of files found in archive: " + untaredFiles.size() );
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
		}

		// if lastModified date was not passed, look if cache index entry exists for collection and retrieve it from there
		if ( collection != null && lastModified == null )
		{
			CollectionCacheIndex collectionCacheIndex = CollectionCacheIndex.retrieve( config, context );
			lastModified = collectionCacheIndex == null ? null : collectionCacheIndex.getLastModified();
			IonLog.d( TAG, "Restoring last_modified from cache index: " + lastModified );
		}

		// clear memory cache because there is no way to selectively delete entries
		MemoryCache.clear();

		// merge files from archive download into collection's cache
		for ( FileWithMeta fileWithMeta : untaredFiles )
		{
			try
			{
				boolean writeSuccess = FileUtils.move( fileWithMeta.fileTemp, fileWithMeta.file, true );
				if ( !writeSuccess )
				{
					throw new IOException( "File could not be moved to its final path '" + fileWithMeta.file.getPath() + "'" );
				}
			}
			catch ( FileMoveException e )
			{
				IonLog.e( "FileMoveException", "URL: " + fileWithMeta.originUrl );
				throw e;
			}
		}

		// remove old pages/media files incl. cache entries (those which are not listed in index json)
		Set<String> archiveUrls = new HashSet<>();
		for ( ArchiveIndex indexEntry : index )
		{
			archiveUrls.add( indexEntry.url );
		}
		// get all index entries stored in the cache index store
		Set<String> outdatedUrls = CacheIndexStore.retrieveAllUrls( config, context );
		// subtract the current index entries - leaving outdated cache index entries
		outdatedUrls.removeAll( archiveUrls );

		for ( String outdatedUrl : outdatedUrls )
		{
			CacheIndexStore.delete( outdatedUrl, config, context );
			try
			{
				// delete all files but archive file
				File file = FilePaths.getFilePath( outdatedUrl, config, context );
				if ( file.exists() && !file.getPath().equals( archiveFile.getPath() ) )
				{
					file.delete();
				}
			}
			catch ( NoIonPagesRequestException e )
			{
				IonLog.e( TAG, "Tried to delete file for URL " + outdatedUrl + "\nBut the URL is not a valid ION Request URL" );
			}
		}


		// add collection to file cache again
		if ( collection != null )
		{
			MemoryCache.saveCollection( collection, config, context );
			try
			{
				saveCollectionToFileCache( config, collection, context );
				CollectionCacheIndex.save( config, context, lastModified, requestTime );
			}
			catch ( IOException e )
			{
				IonLog.e( "ION Archive", "Collection could not be saved." );
				IonLog.ex( e );
			}
		}

		// add archive to file cache again - not the actual file, but the last updated information is required for subsequent archive downloads
		if ( archiveFile != null && archiveFile.exists() && collection != null )
		{
			FileCacheIndex.save( collection.archive, archiveFile, config, null, requestTime, context );
			// delete archiveFile - yes that introduces an inconsistency, but it saves storage space on the other side
			archiveFile.delete();
		}
		else
		{
			String archiveFilePath = archiveFile != null ? archiveFile.getPath() : "n/a";
			IonLog.e( TAG, "Archive Index entry could not be saved. Archive file path: " + archiveFilePath + ", Collection: " + collection );
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

	private static FileWithMeta getFilePath( ArchiveIndex fileInfo, File collectionFolder, IonConfig config, Context context )
	{
		File targetFileTemp;
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
			targetFileTemp = FilePaths.getFilePath( url, config, context, true );
			targetFile = FilePaths.getFilePath( url, config, context, false );
		}
		catch ( NoIonPagesRequestException e )
		{
			IonLog.w( TAG, "URL " + url + " cannot be handled properly. Is it invalid?" );
			File collectionFolderTemp = FilePaths.getTempFilePath( collectionFolder );
			targetFileTemp = new File( collectionFolderTemp, filename );
			targetFile = new File( collectionFolder, filename );
		}
		return new FileWithMeta( targetFile, targetFileTemp, type, url, fileInfo, pageIdentifier );
	}

	public static class FileWithMeta
	{
		File           file;
		File           fileTemp;
		IonRequestType type;
		String         originUrl;
		ArchiveIndex   archiveIndex;
		String         pageIdentifier;

		public FileWithMeta( File file, File fileTemp, IonRequestType type, String originUrl, ArchiveIndex archiveIndex, String pageIdentifier )
		{
			this.file = file;
			this.fileTemp = fileTemp;
			this.type = type;
			this.originUrl = originUrl;
			this.archiveIndex = archiveIndex;
			this.pageIdentifier = pageIdentifier;
		}
	}

	private static File saveCacheIndex( @NonNull FileWithMeta fileWithMeta, Collection collection, String lastModified, DateTime requestTime,
										IonConfig config, Context context )
	{
		IonRequestType type = fileWithMeta.type;
		if ( type == null )
		{
			IonLog.w( TAG, "It could not be determined of which kind the request " + fileWithMeta.archiveIndex.url + " is. Thus, do not create a cache index entry." );
			return fileWithMeta.file;
		}

		switch ( type )
		{
			case COLLECTION:
				CollectionCacheIndex.save( config, context, lastModified, requestTime );
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
				FileCacheIndex.save( fileWithMeta.archiveIndex.url, fileWithMeta.file, config, fileWithMeta.archiveIndex.checksum, requestTime, context );
				break;
		}
		return fileWithMeta.file;
	}
}
