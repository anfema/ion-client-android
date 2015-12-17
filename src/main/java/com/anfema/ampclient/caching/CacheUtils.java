package com.anfema.ampclient.caching;

import android.content.Context;

import com.anfema.ampclient.R;
import com.anfema.ampclient.exceptions.UnknownAmpRequest;
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
	 * Find appropriate file path for media files.
	 * <p>
	 * Do not use for collections and pages – use {@link CacheUtils#getFilePath(String, Context)} instead
	 * Creates folders if the do not exist yet.
	 */
	public static File getMediaFilePath( String url, Context context )
	{
		String filename = FileUtils.calcMD5( url );
		String filePath = context.getFilesDir() + FileUtils.SLASH + context.getString( R.string.files_dir ) + filename;
		return new File( filePath );
	}

	public static File getMediaFilePathExt( String url, Context context )
	{
		String filename = FileUtils.calcMD5( url );
		return new File( context.getExternalFilesDir( null ) + FileUtils.SLASH + context.getString( R.string.files_dir ) + filename );
	}

	/**
	 * Find appropriate file path for collections and pages.
	 * <p>
	 * Do not use for media files – use {@link CacheUtils#getMediaFilePath(String, Context)} instead
	 * Creates folders if the do not exist yet.
	 */
	public static File getFilePath( String url, Context context ) throws UnknownAmpRequest
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
		String filename = FileUtils.calcMD5( url );
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
