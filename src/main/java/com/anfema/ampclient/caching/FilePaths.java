package com.anfema.ampclient.caching;

import android.content.Context;

import com.anfema.ampclient.R;
import com.anfema.ampclient.exceptions.UnknownAmpRequest;
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
	 * <p>
	 * Do not use for collections and pages – use {@link #getJsonFilePath(String, Context)} instead
	 * Creates folders if the do not exist yet.
	 */
	public static File getMediaFilePath( String url, Context context )
	{
		String filename = HashUtils.calcMD5( url );
		String filePath = context.getFilesDir() + FileUtils.SLASH + context.getString( R.string.files_dir ) + filename;
		return new File( filePath );
	}

	public static File getMediaFilePathExt( String url, Context context )
	{
		String filename = HashUtils.calcMD5( url );
		return new File( context.getExternalFilesDir( null ) + FileUtils.SLASH + context.getString( R.string.files_dir ) + filename );
	}

	/**
	 * Find appropriate file path for collections and pages.
	 * <p>
	 * Do not use for media files – use {@link #getMediaFilePath(String, Context)} instead
	 * Creates folders if the do not exist yet.
	 */
	public static File getJsonFilePath( String url, Context context ) throws UnknownAmpRequest
	{
		HttpUrl httpUrl = HttpUrl.parse( url );
		List<String> urlPathSegments = httpUrl.pathSegments();
		List<String> fileNamePathSegments = new ArrayList<>();
		fileNamePathSegments.add( context.getFilesDir() + "" );

		int index = findEndpointPathSegment( urlPathSegments );
		if ( index == -1 )
		{
			throw new UnknownAmpRequest();
		}
		for ( int i = index; i < urlPathSegments.size(); i++ )
		{
			fileNamePathSegments.add( urlPathSegments.get( i ) );
		}

		String folderPath = StringUtils.concatStrings( fileNamePathSegments, FileUtils.SLASH );

		// append file name, which is MD5 hash of url
		String filename = HashUtils.calcMD5( url );
		return new File( folderPath + FileUtils.SLASH + filename );
	}

	/**
	 * find pathSegment indicating endpoint
	 *
	 * @param urlPathSegments
	 * @return
	 */
	private static int findEndpointPathSegment( List<String> urlPathSegments )
	{
		int index = -1;
		for ( int i = 0; i < urlPathSegments.size(); i++ )
		{
			if ( isEndpoint( urlPathSegments.get( i ) ) )
			{
				index = i;
				break;
			}
		}
		return index;
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
		catch ( IllegalArgumentException e )
		{
			return false;
		}
	}
}
