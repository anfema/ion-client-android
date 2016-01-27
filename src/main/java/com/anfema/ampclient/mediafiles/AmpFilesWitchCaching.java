package com.anfema.ampclient.mediafiles;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.caching.FilePaths;
import com.anfema.ampclient.interceptors.AuthorizationHeaderInterceptor;
import com.anfema.ampclient.interceptors.RequestLogger;
import com.anfema.ampclient.utils.FileUtils;
import com.anfema.ampclient.utils.RxUtils;

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
 * <p/>
 * Downloads the response body and stores it into a file.
 * <p/>
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
	public Observable<File> request( HttpUrl url )
	{
		return request( url, null );
	}

	/**
	 * Wraps {@link this#performRequest(HttpUrl, File)} so that it runs completely async.
	 */
	@Override
	public Observable<File> request( HttpUrl url, File targetFile )
	{
		return Observable.just( null )
				.flatMap( o -> performRequest( url, targetFile ) )
				.compose( RxUtils.runOnIoThread() );
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
				throw new IOException( "Unexpected code " + response );
			}

			if ( targetFile != null )
			{
				// use custom target file path
				return Observable.just( writeToLocalStorage( response, targetFile ) );
			}
			// use default target file path
			return Observable.just( writeToLocalStorage( response, url ) );
		}
		catch ( IOException e )
		{
			return Observable.error( e );
		}
	}

	// directly from input stream to file
	private File writeToLocalStorage( Response response, HttpUrl url ) throws IOException
	{
		File targetFile = FilePaths.getMediaFilePath( url.toString(), config, context );
		return writeToLocalStorage( response, targetFile );
	}

	// directly from input stream to file
	private File writeToLocalStorage( Response response, File targetFile ) throws IOException
	{
		// Be aware: using this method empties the response body byte stream. It is not possible to read the response a second time.
		InputStream inputStream = response.body().byteStream();
		File file = FileUtils.writeToFile( inputStream, targetFile );
		inputStream.close();
		return file;
	}
}
