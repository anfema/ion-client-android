package com.anfema.ampclient.caching;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anfema.ampclient.R;
import com.anfema.ampclient.exceptions.NoAmpPagesRequestException;
import com.anfema.ampclient.pages.AmpCallType;
import com.anfema.ampclient.utils.FileUtils;
import com.anfema.ampclient.utils.HashUtils;
import com.anfema.ampclient.utils.StringUtils;
import com.squareup.okhttp.HttpUrl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilePaths
{
	public static File getCollectionFolderPath( String collectionIdentifier, Context context )
	{
		File folder = new File( context.getFilesDir() + FileUtils.SLASH + collectionIdentifier );
		if ( !folder.exists() )
		{
			folder.mkdirs();
		}
		return folder;
	}

	public static File getArchiveFilePath( String collectionIdentifier, Context context )
	{
		return new File( context.getFilesDir() + FileUtils.SLASH + collectionIdentifier + ".archive" );
	}

	/**
	 * Find appropriate file path for media files.
	 * <p/>
	 * Do not use for collections and pages – use {@link #getJsonFilePath(String, Context)} instead
	 * Creates folders if the do not exist yet.
	 */
	public static File getMediaFilePath( String url, Context context )
	{
		File mediaFolderPath = getMediaFolderPath( context );
		String filename = getFileName( url );
		return new File( mediaFolderPath, filename );
	}

	@NonNull
	public static File getMediaFolderPath( Context context )
	{
		return new File( context.getFilesDir() + FileUtils.SLASH + context.getString( R.string.files_dir ) );
	}

	/**
	 * Find appropriate file path for collections and pages.
	 * <p/>
	 * Do not use for media files – use {@link #getMediaFilePath(String, Context)} instead
	 * Creates folders if the do not exist yet.
	 */
	public static File getJsonFilePath( String url, Context context ) throws NoAmpPagesRequestException
	{
		HttpUrl httpUrl = HttpUrl.parse( url );
		List<String> urlPathSegments = httpUrl.pathSegments();
		List<String> fileNamePathSegments = new ArrayList<>();
		fileNamePathSegments.add( context.getFilesDir() + "" );

		int index = findEndpointPathSegment( urlPathSegments );
		if ( index == -1 )
		{
			throw new NoAmpPagesRequestException();
		}
		for ( int i = index + 1; i < urlPathSegments.size(); i++ )
		{
			fileNamePathSegments.add( urlPathSegments.get( i ) );
		}

		String folderPath = StringUtils.concatStrings( fileNamePathSegments, FileUtils.SLASH );

		// append file name, which is MD5 hash of url
		String filename = getFileName( url );
		return new File( folderPath, filename );
	}

	@NonNull
	public static String getFileName( String url )
	{
		return HashUtils.calcMD5( url );
	}

	/**
	 * find pathSegment indicating endpoint
	 *
	 * @param urlPathSegments
	 * @return
	 */
	public static int findEndpointPathSegment( List<String> urlPathSegments )
	{
		for ( int i = 0; i < urlPathSegments.size(); i++ )
		{
			if ( isEndpoint( urlPathSegments.get( i ) ) )
			{
				return i;
			}
		}
		return -1;
	}

	/**
	 * endpoints are defined in enum {@link AmpCallType}
	 *
	 * @param pathSegment
	 * @return
	 */
	private static boolean isEndpoint( String pathSegment )
	{
		try
		{
			AmpCallType.determineCall( pathSegment );
			return true;
		}
		catch ( NoAmpPagesRequestException e )
		{
			return false;
		}
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
}
