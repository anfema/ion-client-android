package com.anfema.ampclient.mediafiles;

import android.content.Context;
import android.support.annotation.Nullable;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.caching.CollectionCacheIndex;
import com.anfema.ampclient.caching.FileCacheIndex;
import com.anfema.ampclient.caching.FilePaths;
import com.anfema.ampclient.exceptions.MediaFileNotAvailableException;
import com.anfema.ampclient.interceptors.AuthorizationHeaderInterceptor;
import com.anfema.ampclient.interceptors.RequestLogger;
import com.anfema.ampclient.utils.FileUtils;
import com.anfema.ampclient.utils.Log;
import com.anfema.ampclient.utils.NetworkUtils;
import com.anfema.ampclient.utils.RxUtils;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;

/**
 * Does not perform calls against a specific API, but takes complete URLs as parameter to perform a GET call to.
 * <p>
 * Downloads the response body and stores it into a file.
 * <p>
 * However, the AMP authorization header is added (in case the URL points to protected media).
 */
public class AmpFilesWitchCaching implements AmpFiles
{
	private       AmpConfig    config;
	private       Context      context;
	private final OkHttpClient client;

	public AmpFilesWitchCaching( AmpConfig config, Context context )
	{
		this.config = config;
		this.context = context;
		OkHttpClient.Builder okHttpClientBuilder = new Builder();
		okHttpClientBuilder.addInterceptor( new AuthorizationHeaderInterceptor( config.authorizationHeaderValue ) );
		okHttpClientBuilder.addInterceptor( new RequestLogger( "Network Request" ) );
		client = okHttpClientBuilder.build();
	}

	/**
	 * Wraps {@link this#performRequest(HttpUrl, File)} so that it runs completely async.
	 */
	@Override
	public Observable<File> request( HttpUrl url, String checksum )
	{
		return request( url, checksum, false, null );
	}

	/**
	 * Wraps {@link this#performRequest(HttpUrl, File)} so that it runs completely async.
	 */
	@Override
	public Observable<File> request( HttpUrl url, String checksum, boolean ignoreCaching, @Nullable File inTargetFile )
	{
		final File targetFile = getTargetFilePath( url, inTargetFile );

		if ( ignoreCaching )
		{
			// force new download, do not create cache index entry
			return requestWithoutCaching( url, targetFile );
		}

		// fetch file from local storage or download it?

		if ( targetFile.exists() && isFileUpToDate( url, checksum ) )
		{
			// retrieve current version from cache
			Log.i( "File Cache Lookup", url.toString() );
			return Observable.just( targetFile );
		}
		else if ( NetworkUtils.isConnected( context ) )
		{
			// download media file
			return Observable.just( null )
					.flatMap( o -> performRequest( url, targetFile ) )
					.doOnNext( file -> FileCacheIndex.save( url.toString(), file, config, null, context ) )
					.compose( RxUtils.runOnIoThread() );
		}
		else if ( targetFile.exists() )
		{
			// TODO notify app that data might be outdated
			// no network: use old version from cache (even if no cache index entry exists)
			Log.i( "File Cache Lookup", url.toString() );
			return Observable.just( targetFile );
		}
		else
		{
			// media file can neither be downloaded nor be found in cache
			return Observable.error( new MediaFileNotAvailableException( url ) );
		}
	}

	private Observable<File> requestWithoutCaching( HttpUrl url, File finalTargetFile )
	{
		return Observable.just( null )
				.flatMap( o -> performRequest( url, finalTargetFile ) )
				.compose( RxUtils.runOnIoThread() );
	}

	private boolean isFileUpToDate( HttpUrl url, String checksum )
	{
		FileCacheIndex fileCacheIndex = FileCacheIndex.retrieve( url.toString(), config.collectionIdentifier, context );
		if ( fileCacheIndex == null )
		{
			return false;
		}

		if ( checksum != null )
		{
			// check with file's checksum
			return !fileCacheIndex.isOutdated( checksum );
		}
		else
		{
			// check with collection's last_modified (previewPage.last_changed would be slightly more precise)
			CollectionCacheIndex collectionCacheIndex = CollectionCacheIndex.retrieve( config, context );
			DateTime collectionLastModified = collectionCacheIndex == null ? null : collectionCacheIndex.getLastModifiedDate();
			DateTime fileLastUpdated = fileCacheIndex.getLastUpdated();
			return collectionLastModified != null && fileLastUpdated != null && !collectionLastModified.isAfter( fileLastUpdated );
		}
	}

	/**
	 * Perform get request and store response body to local storage.
	 *
	 * @param url        source location of content
	 * @param targetFile path, where file is going to be stored. if null, default "/files" directory is used
	 * @return the file with content
	 */
	private Observable<File> performRequest( HttpUrl url, File targetFile )
	{
		// client.setReadTimeout( 30, TimeUnit.SECONDS );

		Request request = new Request.Builder()
				.url( url )
				.build();
		try
		{
			Response response = client.newCall( request ).execute();
			if ( !response.isSuccessful() )
			{
				if ( response.body() != null )
				{
					response.body().close();
				}
				throw new IOException( "Unexpected code: " + response );
			}

			// use custom target file path
			return Observable.just( writeToLocalStorage( response, targetFile ) );
		}
		catch ( IOException e )
		{
			return Observable.error( e );
		}
	}

	/**
	 * write from input stream to file
	 */
	private File writeToLocalStorage( Response response, File targetFile ) throws IOException
	{
		// Be aware: using this method empties the response body byte stream. It is not possible to read the response a second time.
		InputStream inputStream = response.body().byteStream();
		File file = FileUtils.writeToFile( inputStream, targetFile );
		inputStream.close();
		return file;
	}

	/**
	 * Request method does not require that a file path is provided via {@param targetFile}.
	 * If a custom path is provided, it is used. If {@code null} is passed, then the default file path for media files is calculated from the {@param url}.
	 *
	 * @param url        HTTP URL for an AMP internal media file
	 * @param targetFile custom file path (optional)
	 * @return {@param targetFile} or default file path
	 */
	private File getTargetFilePath( HttpUrl url, @Nullable File targetFile )
	{
		if ( targetFile == null )
		{
			targetFile = FilePaths.getMediaFilePath( url.toString(), config, context );
		}
		return targetFile;
	}
}
