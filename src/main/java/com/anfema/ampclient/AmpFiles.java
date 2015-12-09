package com.anfema.ampclient;

import android.content.Context;

import com.anfema.ampclient.authorization.AuthorizationHolder;
import com.anfema.ampclient.caching.CacheUtils;
import com.anfema.ampclient.exceptions.ContextNullPointerException;
import com.anfema.ampclient.interceptors.AuthorizationHeaderInterceptor;
import com.anfema.ampclient.interceptors.RequestLogger;
import com.anfema.ampclient.utils.ContextUtils;
import com.anfema.ampclient.utils.FileUtils;
import com.anfema.ampclient.utils.RxUtils;
import com.google.common.io.Files;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import okio.Buffer;
import okio.BufferedSource;
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

	private static Map<Class<? extends AmpClientConfig>, AmpFiles> fileClientInstances = new HashMap<>();

	public static Observable<AmpFiles> getInstance( Class<? extends AmpClientConfig> configClass, Context context )
	{
		Context appContext = ContextUtils.getApplicationContext( context );

		if ( fileClientInstances == null )
		{
			fileClientInstances = new HashMap<>();
		}

		AmpFiles storedFileClient = fileClientInstances.get( configClass );
		if ( storedFileClient != null )
		{
			if ( storedFileClient.context == null )
			{
				if ( appContext == null )
				{
					Observable.error( new ContextNullPointerException() );
				}
				storedFileClient.context = appContext;
			}
			return Observable.just( storedFileClient );
		}
		return AuthorizationHolder.getAuthHeaderValue( configClass, appContext )
				.map( token -> new AmpFiles( token, appContext ) )
				.doOnNext( okHttpClient -> fileClientInstances.put( configClass, okHttpClient ) );
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
	 * Wraps {@link this#performRequest(HttpUrl)} so that it runs completely async.
	 */
	public Observable<File> request( HttpUrl url )
	{
		return Observable.just( url )
				.flatMap( this::performRequest )
				.compose( RxUtils.applySchedulers() );
	}

	private Observable<File> performRequest( HttpUrl url )
	{
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

			return Observable.just( getFile( response, url ) );
		}
		catch ( IOException e )
		{
			return Observable.error( e );
		}
	}

	// directly from input stream to file
	private File getFile( Response response, HttpUrl url ) throws IOException
	{
		String filePath = CacheUtils.getMediaFilePath( url.toString(), context )/* + ".pdf"*/;
		File targetFile = new File( filePath );
		FileUtils.writeBytesToFile( response, targetFile );
		return targetFile;
	}

	// intermediate: byte array
	private File getFile2( Response response, HttpUrl url ) throws IOException
	{
		ResponseBody responseBody = response.body();
		BufferedSource source = responseBody.source();
		source.request( Long.MAX_VALUE ); // Buffer the entire body.
		Buffer buffer = source.buffer();

		if ( responseBody.contentLength() == 0 )
		{
			return null;
		}

		// TODO empty buffer? work on a copy or original?
		byte[] bytes = buffer/*.clone()*/.readByteArray();
		String filePath = CacheUtils.getMediaFilePath( url.toString(), context )/* + ".pdf"*/;
		File targetFile = new File( filePath );
		FileUtils.createFolders( targetFile.getParentFile() );
		Files.write( bytes, targetFile );
		return targetFile;
	}

	// intermediate: String – approach copied from CachingInterceptor
	private File getFile3( Response response, HttpUrl url ) throws IOException
	{
		String responseBody = getResponseBody( response );
		String filePath = CacheUtils.getMediaFilePath( url.toString(), context )/* + ".pdf"*/;
		File file = new File( filePath );
		FileUtils.writeTextToFile( responseBody, file );
		return file;
	}

	private String getResponseBody( Response response ) throws IOException
	{
		ResponseBody responseBody = response.body();
		BufferedSource source = responseBody.source();
		source.request( Long.MAX_VALUE ); // Buffer the entire body.
		Buffer buffer = source.buffer();

		Charset UTF8 = Charset.forName( "UTF-8" );
		Charset charset = UTF8;
		MediaType contentType = responseBody.contentType();
		if ( contentType != null )
		{
			charset = contentType.charset( UTF8 );
		}

		if ( responseBody.contentLength() == 0 )
		{
			return "";
		}

		return buffer.clone().readString( charset );
	}
}