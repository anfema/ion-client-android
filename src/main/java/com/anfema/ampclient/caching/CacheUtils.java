package com.anfema.ampclient.caching;

import android.content.Context;

import com.anfema.ampclient.exceptions.AmpClientUnknownRequest;
import com.anfema.ampclient.service.AmpCall;
import com.anfema.ampclient.utils.FileUtils;
import com.anfema.ampclient.utils.StringUtils;
import com.squareup.okhttp.HttpUrl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CacheUtils
{
	public static final String PATH_DIVIDER = "/";

	public static boolean isInCache( String filePath )
	{
		File potentialFile = new File( filePath );
		return potentialFile.exists();
	}

	/**
	 * Creates folders if the do not exist yet.
	 */
	public static String getFilePath( String url, Context appContext ) throws AmpClientUnknownRequest
	{
		HttpUrl httpUrl = HttpUrl.parse( url );
		List<String> urlPathSegments = httpUrl.pathSegments();
		List<String> fileNamePathSegments = new ArrayList<>();
		fileNamePathSegments.add( appContext.getFilesDir() + "" );

		int index = findEndpointPathSegment( urlPathSegments );
		if ( index == -1 )
		{
			throw new AmpClientUnknownRequest();
		}

		for ( int i = index; i < urlPathSegments.size(); i++ )
		{
			fileNamePathSegments.add( urlPathSegments.get( i ) );
		}

		String folderPath = StringUtils.concatStrings( fileNamePathSegments, PATH_DIVIDER );

		// create directories if not existing
		FileUtils.createFolders( folderPath );

		// append file name, which is MD5 hash of url
		String filename = FileUtils.calcMD5( url );

		return folderPath + PATH_DIVIDER + filename;
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
	 * endpoints are defined in enum {@link AmpCall}
	 *
	 * @param pathSegment
	 * @return
	 */
	private static boolean isEndpoint( String pathSegment )
	{
		try
		{
			AmpCall.fromPathSegment( pathSegment );
			return true;
		}
		catch ( IllegalArgumentException e )
		{
			return false;
		}
	}
}
