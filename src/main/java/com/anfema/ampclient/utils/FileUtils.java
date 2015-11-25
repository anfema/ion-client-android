package com.anfema.ampclient.utils;

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

import rx.Observable;

public class FileUtils
{
	public static final String SLASH = "/";

	public static void createFolders( String pageFilePath )
	{
		File pageFolder = new File( pageFilePath );
		if ( !pageFolder.exists() )
		{
			Log.d( "FileUtil", "create dirs for: " + pageFilePath );
			pageFolder.mkdirs();
		}
	}

	public static void writeToFile( String content, String filePath ) throws IOException
	{
		File file = new File( filePath );
		writeToFile( content, file );
	}

	/**
	 * Helper function to write content String to a file
	 *
	 * @param content content String to save
	 * @param file    File to save object(s) to
	 */
	public static void writeToFile( String content, File file ) throws IOException
	{
		Log.d( "FileUtil", "write to file: " + file.getPath() );
		if ( file.exists() )
		{
			file.delete();
		}
		file.createNewFile();
		OutputStream outputStream = new FileOutputStream( file, false );
		BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( outputStream ) );
		bufferedWriter.write( content );
		bufferedWriter.close();
	}

	public static Observable<String> readFromFile( String filePath ) throws IOException
	{
		File file = new File( filePath );
		return readFromFile( file );
	}

	public static Observable<String> readFromFile( File file ) throws IOException
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
		} ).compose( RxUtils.applySchedulers() );

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