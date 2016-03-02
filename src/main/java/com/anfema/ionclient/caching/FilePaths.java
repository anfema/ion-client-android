package com.anfema.ionclient.caching;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.exceptions.NoIonPagesRequestException;
import com.anfema.ionclient.pages.IonRequest;
import com.anfema.ionclient.utils.FileUtils;
import com.anfema.ionclient.utils.HashUtils;
import com.anfema.ionclient.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilePaths
{
	public static File getCollectionFolderPath( IonConfig config, Context context )
	{
		File folder = new File( context.getFilesDir() + FileUtils.SLASH + config.collectionIdentifier + FileUtils.SLASH + config.locale );
		if ( !folder.exists() )
		{
			folder.mkdirs();
		}
		return folder;
	}

	public static File getArchiveFilePath( IonConfig config, Context context )
	{
		return new File( context.getFilesDir() + FileUtils.SLASH + config.collectionIdentifier + FileUtils.SLASH + config.locale + ".archive" );
	}

	/**
	 * Finds out if request is a media request or a collection/page request
	 * and delegates to {@link #getMediaFilePath(String, IonConfig, Context, boolean)} or {@link #getJsonFilePath(String, IonConfig, Context, boolean)} respectively.
	 *
	 * @throws NoIonPagesRequestException
	 */
	public static File getFilePath( String url, IonConfig config, Context context ) throws NoIonPagesRequestException
	{
		return getFilePath( url, config, context, false );
	}

	public static File getFilePath( String url, IonConfig config, Context context, boolean tempFolder ) throws NoIonPagesRequestException
	{
		if ( IonRequest.isMediaRequestUrl( url ) )
		{
			return getMediaFilePath( url, config, context, tempFolder );
		}
		else
		{
			return getJsonFilePath( url, config, context, tempFolder );
		}
	}

	/**
	 * Find appropriate file path for media files.
	 * <p/>
	 * Do not use for collections and pages – use {@link #getJsonFilePath(String, IonConfig, Context, boolean)} instead
	 * Creates folders if the do not exist yet.
	 */
	public static File getMediaFilePath( String url, IonConfig config, Context context )
	{
		return getMediaFilePath( url, config, context, false );
	}

	public static File getMediaFilePath( String url, IonConfig config, Context context, boolean tempCollectionFolder )
	{
		File mediaFolderPath = getMediaFolderPath( config, context, tempCollectionFolder );
		String filename = getFileName( url );
		return new File( mediaFolderPath, filename );
	}

	@NonNull
	public static File getMediaFolderPath( IonConfig config, Context context, boolean tempCollectionFolder )
	{
		return new File( context.getFilesDir() + FileUtils.SLASH + config.collectionIdentifier + FileUtils.SLASH + appendTemp( "media", tempCollectionFolder ) );
	}

	/**
	 * Find appropriate file path for collections and pages.
	 * <p/>
	 * Do not use for media files – use {@link #getMediaFilePath(String, IonConfig, Context, boolean)} instead
	 * Creates folders if the do not exist yet.
	 */
	public static File getJsonFilePath( String url, IonConfig config, Context context ) throws NoIonPagesRequestException
	{
		return getJsonFilePath( url, config, context, false );
	}

	/**
	 * Find appropriate file path for collections and pages.
	 * <p/>
	 * Do not use for media files – use {@link #getMediaFilePath(String, IonConfig, Context, boolean)} instead
	 * Creates folders if the do not exist yet.
	 */
	public static File getJsonFilePath( String url, IonConfig config, Context context, boolean tempFolder ) throws NoIonPagesRequestException
	{
		List<String> fileNamePathSegments = new ArrayList<>();
		// internal storage path for the app on local storage
		fileNamePathSegments.add( context.getFilesDir() + "" );

		// relative path corresponds to URL without base URL
		String relativeUrlPath = url.replace( config.baseUrl, "" );
		String[] urlPathSegments = relativeUrlPath.split( "/" );

		// urlPathSegments length is 2 for collection call and 3 for page call
		if ( urlPathSegments.length < 2 || urlPathSegments.length > 3 )
		{
			throw new NoIonPagesRequestException( url );
		}

		// relative folder path is deferred from urlPathSegments, only locale and collection identifier are switched
		List<String> relativeFilePath = new ArrayList<>();
		relativeFilePath.add( urlPathSegments[ 1 ] );
		relativeFilePath.add( appendTemp( urlPathSegments[ 0 ], tempFolder ) );
		if ( urlPathSegments.length > 2 )
		{
			relativeFilePath.add( urlPathSegments[ 2 ] );
		}


		// append relative path for spe
		fileNamePathSegments.addAll( relativeFilePath );

		String folderPath = StringUtils.concatStrings( fileNamePathSegments, FileUtils.SLASH );

		// append file name, which is MD5 hash of url
		String filename = getFileName( url );
		return new File( folderPath, filename );
	}

	@NonNull
	public static String getFileName( String url )
	{
		return HashUtils.getMD5Hash( url );
	}

	@NonNull
	public static File getTempFilePath( File file )
	{
		File fileTemp = getTempName( file );
		FileUtils.createDir( file.getParentFile() );
		return fileTemp;
	}

	@NonNull
	public static File getTempFolderPath( File folder )
	{
		File folderTemp = getTempName( folder );
		FileUtils.createDir( folder );
		return folderTemp;
	}

	@NonNull
	private static File getTempName( File file )
	{
		File folderTemp = new File( file.getPath() + "_temp" );
		FileUtils.reset( folderTemp );
		return folderTemp;
	}

	private static String appendTemp( String path, boolean appendTemp )
	{
		if ( appendTemp )
		{
			path += "_temp";
		}
		return path;
	}
}
