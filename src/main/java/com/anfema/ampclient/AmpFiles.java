package com.anfema.ampclient;

import android.content.Context;

import com.anfema.ampclient.caching.FilePaths;
import com.anfema.ampclient.exceptions.AuthorizationHeaderValueIsNullException;
import com.anfema.ampclient.interceptors.AuthorizationHeaderInterceptor;
import com.anfema.ampclient.interceptors.RequestLogger;
import com.anfema.ampclient.utils.ContextUtils;
import com.anfema.ampclient.utils.FileUtils;
import com.anfema.ampclient.utils.RxUtils;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;

/**
 * Does not perform calls against a specific API, but takes complete URLs as parameter to perform a GET call to.
 * <p/>
 * Downloads the response body and stores it into a file.
 * <p/>
 * However, the AMP authorization header is added (in case the URL points to protected media).
 */
public class AmpFiles
{
	/// Multiton

	// key: authorization header value
	private static Map<String, AmpFiles> fileClientInstances = new HashMap<>();

	public static AmpFiles getInstance( String authorizationHeaderValue, Context context )
	{
		if ( authorizationHeaderValue == null )
		{
			throw new AuthorizationHeaderValueIsNullException();
		}

		AmpFiles storedFileClient = fileClientInstances.get( authorizationHeaderValue );
		if ( storedFileClient != null && storedFileClient.context != null )
		{
			return storedFileClient;
		}

		Context appContext = ContextUtils.getApplicationContext( context );
		AmpFiles ampFiles = new AmpFiles( authorizationHeaderValue, appContext );
		fileClientInstances.put( authorizationHeaderValue, ampFiles );
		return ampFiles;
	}

	/// Multiton END

	private       Context      context;
	private final OkHttpClient client;

	private AmpFiles( String authHeaderValue, Context context )
	{
		this.context = context;
		client = new OkHttpClient();
		client.interceptors().add( new AuthorizationHeaderInterceptor( authHeaderValue ) );
		client.interceptors().add( new RequestLogger( "Network Request" ) );
	}

	/**
	 * Wraps {@link this#performRequest(HttpUrl, File)} so that it runs completely async.
	 */
	public Observable<File> request( HttpUrl url )
	{
		return request( url, null );
	}

	/**
	 * Wraps {@link this#performRequest(HttpUrl, File)} so that it runs completely async.
	 */
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
		File targetFile = FilePaths.getMediaFilePath( url.toString(), context )/* + ".pdf"*/;
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
