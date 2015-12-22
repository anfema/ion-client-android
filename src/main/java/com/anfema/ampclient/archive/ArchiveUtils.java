package com.anfema.ampclient.archive;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.caching.FilePaths;
import com.anfema.ampclient.exceptions.NoAmpPagesRequestException;
import com.anfema.ampclient.pages.AmpCallType;
import com.anfema.ampclient.pages.PagesUrls;
import com.anfema.ampclient.serialization.GsonHolder;
import com.anfema.ampclient.utils.FileUtils;
import com.anfema.ampclient.utils.Log;
import com.anfema.ampclient.utils.RxUtils;
import com.anfema.ampclient.utils.StringUtils;
import com.squareup.okhttp.HttpUrl;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

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

import rx.Observable;

public class ArchiveUtils
{
	private static final String TAG = "ArchiveUtils";

	public static Observable<File> unTar( File archiveFile, AmpConfig config, Context context )
	{
		return Observable.just( null )
				.flatMap( o -> {
					try
					{
						return Observable.from( performUnTar( archiveFile, config, context ) );
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
	 * @return The {@link List} of {@link File}s with the untared content.
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ArchiveException
	 */
	private static List<File> performUnTar( File archiveFile, AmpConfig config, Context context ) throws FileNotFoundException, IOException, ArchiveException
	{
		File collectionFolder = FilePaths.getCollectionFolderPath( config.collectionIdentifier, context );

		Log.d( TAG, String.format( "Untaring %s to dir %s.", archiveFile.getPath(), collectionFolder.getPath() ) );

		File collectionFolderTemp = FilePaths.getTempFilePath( collectionFolder );

		final List<File> untaredFiles = new LinkedList<>();
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

				File targetFile = getFilePath( fileInfo.url, collectionFolderTemp, context );
				Log.d( TAG, String.format( "Untaring: Write file %s.", targetFile.getPath() ) );
				FileUtils.createDir( targetFile.getParentFile() );

				targetFile = FileUtils.writeToFile( debInputStream, targetFile );

				if ( targetFile != null )
				{
					untaredFiles.add( targetFile );
				}
			}
		}

		// otherwise we would delete the collection Json we already downloaded before
		keepCollectionJson( collectionFolderTemp, config, context );

		// finished reading TAR archive
		debInputStream.close();
		archiveFile.delete();

		// replace collection folder (containing json files)
		FileUtils.move( collectionFolderTemp, collectionFolder, true );

		return untaredFiles;
	}

	private static File getFilePath( String url, File collectionFolderTemp, Context context )
	{
		File targetFile;
		try
		{
			// check URL is a collections or pages call
			HttpUrl httpUrl = HttpUrl.parse( url );
			AmpCallType.determineCall( httpUrl );

			// build json file path with collection temp folder
			String filename = FilePaths.getFileName( url );

			List<String> urlPathSegments = httpUrl.pathSegments();
			int endpointIndex = FilePaths.findEndpointPathSegment( urlPathSegments );
			if ( endpointIndex > -1 && urlPathSegments.size() > endpointIndex + 1 )
			{
				List<String> remainingPathSegments = new ArrayList<>();
				for ( int i = endpointIndex + 2; i < urlPathSegments.size(); i++ )
				{
					remainingPathSegments.add( urlPathSegments.get( i ) );
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
		return targetFile;
	}

	private static void keepCollectionJson( File collectionFolderTemp, AmpConfig config, Context context )
	{
		// keep the collection json
		String collectionUrl = PagesUrls.getCollectionUrl( config );
		try
		{
			File collectionJson = FilePaths.getJsonFilePath( collectionUrl, context );
			if ( collectionJson.exists() )
			{
				String filename = FilePaths.getFileName( collectionUrl );
				collectionJson.renameTo( new File( collectionFolderTemp, filename ) );
			}
		}
		catch ( NoAmpPagesRequestException e )
		{
			Log.ex( e );
		}
	}
}
