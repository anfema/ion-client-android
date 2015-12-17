package com.anfema.ampclient.utils;

import com.squareup.okhttp.Response;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;

public class FileUtils
{
	public static volatile Map<String, Object> ioLocks = new HashMap<>();

	public static final String SLASH = "/";

	public static synchronized boolean createFolders( File dir )
	{
		if ( dir.exists() )
		{
			return true;
		}
		Log.d( "FileUtil", "create dirs for: " + dir );
		return dir.mkdirs();
	}

	/**
	 * Helper function to write content String to a file.
	 * <p>
	 * Should not be called from main thread.
	 *
	 * @param content content String to save
	 * @param file    File to save object(s) to
	 */
	public static synchronized void writeTextToFile( String content, File file ) throws IOException
	{
		Log.d( "FileUtil", "write to file: " + file.getPath() );
		if ( file.exists() )
		{
			file.delete();
		}
		else
		{
			createFolders( file.getParentFile() );
		}
		file.createNewFile();

		OutputStream outputStream = new FileOutputStream( file, false );
		BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( outputStream ) );
		bufferedWriter.write( content );
		bufferedWriter.close();
	}

	/**
	 * Be aware: using this method empties the response body byte stream. It is not possible to read the response a second time.
	 *
	 * @param file target file
	 * @throws IOException
	 */
	public static synchronized void writeBytesToFile( Response response, File file ) throws IOException
	{
		Log.d( "FileUtil", "write to file: " + file.getPath() );
		if ( file.exists() )
		{
			file.delete();
		}
		else
		{
			createFolders( file.getParentFile() );
		}
		file.createNewFile();

		OutputStream stream = new BufferedOutputStream( new FileOutputStream( file ) );
		try
		{
			int bufferSize = 1024;
			byte[] buffer = new byte[ bufferSize ];

			int len;
			while ( ( len = response.body().byteStream().read( buffer ) ) != -1 )
			{
				stream.write( buffer, 0, len );
			}
		}
		finally
		{
			stream.close();
		}
	}

	public static synchronized Observable<String> readFromFile( File file ) throws IOException
	{
		return Observable.just( file ).flatMap( o ->
		{
			StringBuilder text = new StringBuilder();
			BufferedReader br = null;
			try
			{
				br = new BufferedReader( new FileReader( file ) );
				String line;

				while ( ( line = br.readLine() ) != null )
				{
					text.append( line );
					text.append( '\n' );
				}
				br.close();
				return Observable.just( text.toString() );
			}
			catch ( IOException e )
			{
				return Observable.error( e );
			}
		} ).compose( RxUtils.runOnIoThread() );

	}

	public static void deleteRecursive( File fileOrDirectory )
	{
		if ( fileOrDirectory.isDirectory() )
		{
			for ( File child : fileOrDirectory.listFiles() )
			{
				deleteRecursive( child );
			}
		}
		fileOrDirectory.delete();
	}

	public static String calcMD5( String input )
	{
		MessageDigest md;
		try
		{
			md = MessageDigest.getInstance( "MD5" );
		}
		catch ( NoSuchAlgorithmException e )
		{
			Log.ex( e );
			return "-1";
		}
		md.update( input.getBytes() );

		byte byteData[] = md.digest();

		//convert the byte to hex format
		StringBuffer hexString = new StringBuffer();
		for ( byte aByte : byteData )
		{
			String hex = Integer.toHexString( 0xff & aByte );
			if ( hex.length() == 1 )
			{
				hexString.append( '0' );
			}
			hexString.append( hex );
		}
		return hexString.toString();
	}
}