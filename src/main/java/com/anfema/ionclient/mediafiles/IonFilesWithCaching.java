package com.anfema.ionclient.mediafiles;

import android.content.Context;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.IonConfig.CachingStrategy;
import com.anfema.ionclient.caching.CacheCompatManager;
import com.anfema.ionclient.caching.FilePaths;
import com.anfema.ionclient.caching.index.CollectionCacheIndex;
import com.anfema.ionclient.caching.index.FileCacheIndex;
import com.anfema.ionclient.exceptions.FileNotAvailableException;
import com.anfema.ionclient.exceptions.HttpException;
import com.anfema.ionclient.interceptors.AdditionalHeadersInterceptor;
import com.anfema.ionclient.interceptors.AuthorizationHeaderInterceptor;
import com.anfema.ionclient.pages.models.contents.Downloadable;
import com.anfema.ionclient.utils.DateTimeUtils;
import com.anfema.ionclient.utils.FileUtils;
import com.anfema.ionclient.utils.IonLog;
import com.anfema.ionclient.utils.PendingDownloadHandler;
import com.anfema.utils.NetworkUtils;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import kotlin.jvm.functions.Function0;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

import static com.anfema.ionclient.okhttp.IonOkHttpKt.okHttpClient;
import static com.anfema.ionclient.utils.IonLog.INFO;
import static com.anfema.ionclient.utils.IonLog.VERBOSE;

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
	private final Context                               context;
	private       OkHttpClient                          client;
	private final PendingDownloadHandler<HttpUrl, File> runningDownloads;

	public IonFilesWithCaching( IonConfig config, Context context )
	{
		this.config = config;
		this.context = context;
		client = okHttpClient( this.config::getAuthorizationHeaderValue, config.additionalHeaders, config.networkTimeout );
		runningDownloads = new PendingDownloadHandler<>();
	}

	@Override
	public void updateConfig( IonConfig config )
	{
		this.config = config;
		OkHttpClient.Builder newClient = client.newBuilder();
		NetworkUtils.applyTimeout( newClient, config.networkTimeout );
		this.client = newClient.build();
	}

	/**
	 * Convenience method: request a {@link Downloadable} content
	 *
	 * @see #request(HttpUrl, String)
	 */
	@Override
	public Single<FileWithStatus> request( Downloadable content )
	{
		return request( HttpUrl.parse( content.getUrl() ), content.getChecksum() );
	}

	/**
	 * Convenience method: caching is enabled, custom destination file path is used.
	 *
	 * @see #request(HttpUrl, String, boolean, File)
	 */
	@Override
	public Single<FileWithStatus> request( HttpUrl url, String checksum )
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
	public Single<FileWithStatus> request( HttpUrl url, String checksum, boolean ignoreCaching, @Nullable File inTargetFile )
	{
		return request( url, url, checksum, ignoreCaching, inTargetFile );
	}

	/**
	 * Retrieve a file through its URL either from file cache or with a network request. The result can be cached for further requests.
	 *
	 * @param url           File location is defined this HTTP URL.
	 * @param downloadUrl   The actual HTTP URL used to make a network request, it can differ from lookupUrl (e.g. by additional query parameter lastUpdated)
	 * @param checksum      checksum of the current file on server
	 * @param ignoreCaching If set to true file is retrieved through a network request and not stored in cache.
	 * @param inTargetFile  Optionally, a custom file path can be provided. If {@code null}, the default scheme is used.
	 * @return file is retrieved when subscribed to the the result of this async operation.
	 */
	@Override
	public Single<FileWithStatus> request( HttpUrl url, HttpUrl downloadUrl, String checksum, boolean ignoreCaching, @Nullable File inTargetFile )
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
				return authenticatedFileRequest( downloadUrl, targetFile )
						.map( file -> new FileWithStatus( file, FileStatus.NETWORK ) )
						.subscribeOn( Schedulers.io() );
			}
			else
			{
				return Single.error( new FileNotAvailableException( url ) );
			}
		}

		// fetch file from local storage or download it?

		if ( targetFile.exists() && isFileUpToDate( url, checksum ) )
		{
			// retrieve current version from cache
			IonLog.i( "File Cache Lookup", url.toString() );
			return Single.just( targetFile )
					.map( file -> new FileWithStatus( file, FileStatus.DISK ) );
		}
		else
		{
			if ( networkAvailable )
			{
				DateTime requestTime = DateTimeUtils.now();
				// download media file
				Single<File> downloadSingle = authenticatedFileRequest( downloadUrl, targetFile )
						.doOnSuccess( file -> FileCacheIndex.save( url.toString(), file, config, null, requestTime, context ) )
						.subscribeOn( Schedulers.io() )
						.doOnSuccess( file -> runningDownloads.finished( url ) );
				return runningDownloads.starting( url, downloadSingle.toObservable() ).singleOrError()
						.map( file -> new FileWithStatus( file, FileStatus.NETWORK ) );
			}
			else if ( targetFile.exists() )
			{
				// no network: use old version from cache (even if no cache index entry exists)
				IonLog.i( "File Cache Lookup", url.toString() );
				return Single.just( targetFile )
						.map( file -> new FileWithStatus( file, FileStatus.DISK_OUTDATED ) );
			}
			else
			{
				// media file can neither be downloaded nor be found in cache
				return Single.error( new FileNotAvailableException( url ) );
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
			return collectionLastModified != null && !collectionLastModified.isAfter( fileLastUpdated );
		}
	}

	/**
	 * Request and store response body to local storage.
	 *
	 * @param url        source location of content
	 * @param targetFile path, where file is going to be stored. if null, default "/files" directory is used
	 * @return the file with content
	 */
	private Single<File> authenticatedFileRequest( HttpUrl url, File targetFile )
	{
		return config.authenticatedRequest( () -> performRequest( url ) )
				.flatMap( response -> writeToLocalStorage( response, targetFile ) );
	}

	/**
	 * Perform get request
	 */
	@NonNull
	private Single<Response> performRequest( HttpUrl url )
	{
		// client.setReadTimeout( 30, TimeUnit.SECONDS );

		Request request = new Request.Builder().url( url ).build();

		try
		{
			Response response = client.newCall( request ).execute();
			if ( !response.isSuccessful() )
			{
				ResponseBody responseBody = response.body();
				if ( responseBody != null )
				{
					responseBody.close();
				}
				return Single.error( new HttpException( response.code(), response.message() ) );
			}

			// use custom target file path
			return Single.just( response );
		}
		catch ( IOException e )
		{
			return Single.error( e );
		}
	}

	/**
	 * write from input stream to file
	 */
	private Single<File> writeToLocalStorage( Response response, File targetFile )
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
				inputStream.close();
			}
		}
		catch ( IOException e )
		{
			return Single.error( e );
		}
		if ( file == null )
		{
			return Single.error( new IOException( "Failure writing " + targetFile.getPath() + " to local storage." ) );
		}
		return Single.just( file );
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
