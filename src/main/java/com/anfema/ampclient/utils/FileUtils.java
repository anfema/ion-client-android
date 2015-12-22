package com.anfema.ampclient.utils;

import com.anfema.ampclient.caching.FilePaths;
import com.anfema.ampclient.exceptions.FileMoveException;

import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import rx.Observable;

public class FileUtils
{
	public static final String TAG = "FileUtils";

	public static final String SLASH = "/";

	public static File writeToFile( InputStream inputStream, File file ) throws IOException
	{
		Log.d( TAG, "write to file: " + file.getPath() );

		File fileTemp = FilePaths.getTempFilePath( file );

		OutputStream outputStream = new BufferedOutputStream( new FileOutputStream( fileTemp ) );
		IOUtils.copy( inputStream, outputStream );
		outputStream.close();

		boolean writeSuccess = move( fileTemp, file, true );
		return writeSuccess ? file : null;
	}

	/**
	 * Helper function to write content String to a file.
	 * <p/>
	 * Should not be called from main thread.
	 *
	 * @param content content String to save
	 * @param file    File to save object(s) to
	 */
	public static File writeTextToFile( String content, File file ) throws IOException
	{
		Log.d( TAG, "write to file: " + file.getPath() );

		File fileTemp = FilePaths.getTempFilePath( file );

		OutputStream outputStream = new FileOutputStream( fileTemp, false );
		BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( outputStream ) );
		bufferedWriter.write( content );
		bufferedWriter.close();

		boolean writeSuccess = move( fileTemp, file, true );
		return writeSuccess ? file : null;
	}

	public static Observable<String> readFromFile( File file ) throws IOException
	{
		return Observable.just( null )
				.flatMap( o ->
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

	/**
	 * Move file or folder from source path to target path.
	 *
	 * @param source         defining the content
	 * @param target         defining the path
	 * @param forceOverwrite If set to {@code false} and target exists move operation is not performed. Move operation is always performed if set to {@code true}.
	 * @return {@code true} if move operation had been performed
	 */
	public static boolean move( File source, File target, boolean forceOverwrite )
	{
		if ( !forceOverwrite && target.exists() )
		{
			return false;
		}

		if ( source.isDirectory() && target.exists() && !target.isDirectory() || !source.isDirectory() && target.isDirectory() )
		{
			throw new FileMoveException( source, target );
		}

		if ( source.isDirectory() )
		{
			deleteRecursive( target );
			createDir( target );
		}
		else
		{
			reset( target );
			if ( source.isDirectory() )
			{
				createDir( target );
			}
			else
			{
				createDir( target.getParentFile() );
			}
		}

		return source.renameTo( target );
	}

	/**
	 * Reset file if exists
	 */
	public static void reset( File file )
	{
		if ( file.exists() )
		{
			file.delete();
		}
		else
		{
			createDir( file.getParentFile() );
		}
	}

	/**
	 * create all missing directories that exist in this path
	 *
	 * @return {@code true} if the directory was created,
	 * {@code false} on failure or if the directory already existed.
	 */
	public static synchronized boolean createDir( File dir )
	{
		if ( dir.exists() )
		{
			return false;
		}
		Log.d( TAG, "create dirs for: " + dir );
		return dir.mkdirs();
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
}