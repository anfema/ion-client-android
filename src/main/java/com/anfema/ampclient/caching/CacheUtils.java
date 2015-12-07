package com.anfema.ampclient.caching;

import android.content.Context;

import com.anfema.ampclient.exceptions.AmpClientUnknownRequest;
import com.anfema.ampclient.service.AmpCall;
import com.anfema.ampclient.utils.FileUtils;
import com.anfema.ampclient.utils.StringUtils;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class CacheUtils
{

	public static boolean isInCache( String filePath )
	{
		File potentialFile = new File( filePath );
		return potentialFile.exists();
	}

	public static Observable<Response> createCacheResponse( Request request, String filePath ) throws IOException
	{
		return FileUtils.readFromFile( filePath ).map( responseBody -> new Response.Builder()
				.protocol( Protocol.HTTP_1_1 )
				.request( request )
				.code( HttpURLConnection.HTTP_OK )
				.body( ResponseBody.create( MediaType.parse( com.anfema.ampclient.utils.MediaType.JSON_UTF_8.toString() ), responseBody ) )
				.build() );
	}

	/**
	 * Creates folders if the do not exist yet.
	 */
	public static String getFilePath( String url, Context context ) throws AmpClientUnknownRequest
	{
		HttpUrl httpUrl = HttpUrl.parse( url );
		List<String> urlPathSegments = httpUrl.pathSegments();
		List<String> fileNamePathSegments = new ArrayList<>();
		fileNamePathSegments.add( context.getFilesDir() + "" );

		int index = findEndpointPathSegment( urlPathSegments );
		if ( index == -1 )
		{
			throw new AmpClientUnknownRequest();
		}

		for ( int i = index; i < urlPathSegments.size(); i++ )
		{
			fileNamePathSegments.add( urlPathSegments.get( i ) );
		}

		String folderPath = StringUtils.concatStrings( fileNamePathSegments, FileUtils.SLASH );
		// append file name, which is MD5 hash of url
		String filename = FileUtils.calcMD5( url );
		String filePath = folderPath + FileUtils.SLASH + filename;

		// create directories if not existing
		FileUtils.createFolders( filePath );

		return filePath;
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