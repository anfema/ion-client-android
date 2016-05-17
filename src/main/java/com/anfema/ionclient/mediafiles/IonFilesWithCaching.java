package com.anfema.ionclient.mediafiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.IonConfig.CachingStrategy;
import com.anfema.ionclient.caching.CacheCompatManager;
import com.anfema.ionclient.caching.FilePaths;
import com.anfema.ionclient.caching.index.CollectionCacheIndex;
import com.anfema.ionclient.caching.index.FileCacheIndex;
import com.anfema.ionclient.exceptions.FileNotAvailableException;
import com.anfema.ionclient.interceptors.AuthorizationHeaderInterceptor;
import com.anfema.ionclient.interceptors.RequestLogger;
import com.anfema.ionclient.pages.models.contents.Downloadable;
import com.anfema.ionclient.utils.FileUtils;
import com.anfema.utils.Log;
import com.anfema.utils.NetworkUtils;
import com.anfema.ionclient.utils.PendingDownloadHandler;
import com.anfema.ionclient.utils.RxUtils;

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
 * However, the ION authorization header is added (in case the URL points to protected media).
 */
public class IonFilesWithCaching implements IonFiles
{
	private       IonConfig                             config;
	private       Context                               context;
	private final OkHttpClient                          client;
	private       PendingDownloadHandler<HttpUrl, File> runningDownloads;

	public IonFilesWithCaching( IonConfig config, Context context )
	{
		this.config = config;
		this.context = context;
		OkHttpClient.Builder okHttpClientBuilder = new Builder();
		okHttpClientBuilder.addInterceptor( new AuthorizationHeaderInterceptor( this.config::getAuthorizationHeaderValue ) );
		okHttpClientBuilder.addInterceptor( new RequestLogger( "Network Request" ) );
		// disable okHttp disk cache because it would store duplicate and un-used data because ION client uses its own cache
		okHttpClientBuilder.cache( null );
		client = okHttpClientBuilder.build();
		runningDownloads = new PendingDownloadHandler<>();
	}

	@Override
	public void updateConfig( IonConfig config )
	{
		this.config = config;
	}

	/**
	 * Convenience method: request a {@link Downloadable} content
	 *
	 * @see #request(HttpUrl, String)
	 */
	@Override
	public Observable<File> request( Downloadable content )
	{
		return request( HttpUrl.parse( content.getUrl() ), content.getChecksum() );
	}

	/**
	 * Convenience method: caching is enabled, custom destination file path is used.
	 *
	 * @see #request(HttpUrl, String, boolean, File)
	 */
	@Override
	public Observable<File> request( HttpUrl url, String checksum )
	{
		return request( url, checksum, false, null );
	}

	/**
	 * Retrieve a file through its URL either from file cache or with a network request. The result can be cached for further requests.
	 *
	 * @param url           File location is defined through a HTTP URL.
	 * @param checksum      checksum of the current file on server
	 * @param ignoreCaching If set to true file is retrieved through a network request and not stored in cache.
	 * @param inTargetFile  Optionally, a custom file path can be provided. If {@code null}, the default scheme is used.
	 * @return file is retrieved when subscribed to the the result of this async operation.
	 */
	@Override
	public Observable<File> request( HttpUrl url, String checksum, boolean ignoreCaching, @Nullable File inTargetFile )
	{
		// clear incompatible cache
		CacheCompatManager.cleanUp( context );

		boolean networkAvailable = NetworkUtils.isConnected( context ) && IonConfig.cachingStrategy != CachingStrategy.STRICT_OFFLINE;
		final File targetFile = getTargetFilePath( url, inTargetFile );

		if ( ignoreCaching )
		{
			if ( networkAvailable )
			{
				// force new download, do not create cache index entry
				return authenticatedFileRequest( url, targetFile )
						.compose( RxUtils.runOnIoThread() );
			}
			else
			{
				return Observable.error( new FileNotAvailableException( url ) );
			}
		}

		// fetch file from local storage or download it?

		if ( targetFile.exists() && isFileUpToDate( url, checksum ) )
		{
			// retrieve current version from cache
			Log.i( "File Cache Lookup", url.toString() );
			return Observable.just( targetFile );
		}
		else
		{
			if ( networkAvailable )
			{
				// download media file
				Observable<File> downloadObservable = authenticatedFileRequest( url, targetFile )
						.doOnNext( file -> FileCacheIndex.save( url.toString(), file, config, null, context ) )
						.compose( RxUtils.runOnIoThread() )
						.doOnNext( file -> runningDownloads.finished( url ) );
				return runningDownloads.starting( url, downloadObservable );
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
				return Observable.error( new FileNotAvailableException( url ) );
			}
		}
	}

	private boolean isFileUpToDate( HttpUrl url, String checksum )
	{
		FileCacheIndex fileCacheIndex = FileCacheIndex.retrieve( url.toString(), config, context );
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
	 * Request and store response body to local storage.
	 *
	 * @param url        source location of content
	 * @param targetFile path, where file is going to be stored. if null, default "/files" directory is used
	 * @return the file with content
	 */
	private Observable<File> authenticatedFileRequest( HttpUrl url, File targetFile )
	{
		return config.authenticatedRequest( () -> performRequest( url ) )
				.flatMap( response -> writeToLocalStorage( response, targetFile ) );
	}

	/**
	 * Perform get request
	 */
	@NonNull
	private Observable<Response> performRequest( HttpUrl url )
	{
		// client.setReadTimeout( 30, TimeUnit.SECONDS );

		Request request = new Request.Builder().url( url ).build();

		try
		{
			Response response = client.newCall( request ).execute();
			if ( !response.isSuccessful() )
			{
				if ( response.body() != null )
				{
					response.body().close();
				}
				return Observable.error( new IOException( "Unexpected code: " + response ) );
			}

			// use custom target file path
			return Observable.just( response );
		}
		catch ( IOException e )
		{
			return Observable.error( e );
		}
	}

	/**
	 * write from input stream to file
	 */
	private Observable<File> writeToLocalStorage( Response response, File targetFile )
	{
		// Be aware: using this method empties the response body byte stream. It is not possible to read the response a second time.
		InputStream inputStream = response.body().byteStream();
		File file;
		try
		{
			try
			{
				file = FileUtils.writeToFile( inputStream, targetFile );
			}
			finally
			{
				if ( inputStream != null )
				{
					inputStream.close();
				}
			}
		}
		catch ( IOException e )
		{
			return Observable.error( e );
		}
		return Observable.just( file );
	}

	/**
	 * Request method does not require that a file path is provided via {@param targetFile}.
	 * If a custom path is provided, it is used. If {@code null} is passed, then the default file path for media files is calculated from the {@param url}.
	 *
	 * @param url        HTTP URL for an ION internal media file
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
