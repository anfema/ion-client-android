package com.anfema.ionclient.utils;

import com.anfema.ionclient.caching.FilePaths;
import com.anfema.ionclient.exceptions.FileMoveException;

import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;

public class FileUtils
{
	public static final String TAG   = "FileUtils";
	public static final String SLASH = "/";

	/// lock mechanism ensures file write operations on same file do not interfere

	static class LockWithCounter
	{
		int counter;

		public LockWithCounter()
		{
			counter = 0;
		}
	}

	private static Map<File, LockWithCounter> ongoingWriteOperations = new HashMap<>();

	private static synchronized void releaseLock( File file )
	{
		LockWithCounter lock = ongoingWriteOperations.get( file );
		lock.counter--;

		if ( lock.counter <= 0 )
		{
			ongoingWriteOperations.remove( file );
		}
		// Log.d( "Lock Mechanism", "Released lock for file " + file.getPath() + ". counter: " + lock.counter );
	}

	private synchronized static LockWithCounter obtainLock( File file )
	{
		LockWithCounter lock = ongoingWriteOperations.get( file );
		if ( lock == null )
		{
			lock = new LockWithCounter();
			ongoingWriteOperations.put( file, lock );
		}
		lock.counter++;
		// Log.d( "Lock Mechanism", "Obtained lock for file " + file.getPath() + ". counter: " + lock.counter );
		return lock;
	}

	// END lock mechanism


	/**
	 * Write from an input stream to a file.
	 * <p>
	 * Should not be called from main thread.
	 *
	 * @param inputStream source of the bytes to write into file
	 * @param targetFile  file path to save data to
	 */
	public static File writeToFile( InputStream inputStream, File targetFile ) throws IOException
	{
		boolean writeSuccess;
		synchronized ( obtainLock( targetFile ) )
		{
			Log.d( TAG, "write to file: " + targetFile.getPath() );

			File fileTemp = FilePaths.getTempFilePath( targetFile );

			OutputStream outputStream = new BufferedOutputStream( new FileOutputStream( fileTemp ) );
			IOUtils.copy( inputStream, outputStream );
			outputStream.close();

			writeSuccess = move( fileTemp, targetFile, true );

			releaseLock( targetFile );
		}
		return writeSuccess ? targetFile : null;
	}

	/**
	 * Convenience method to write content String to a file.
	 * <p>
	 * Should not be called from main thread.
	 *
	 * @param content    content String to save
	 * @param targetFile file path to save data to
	 */
	public static File writeTextToFile( String content, File targetFile ) throws IOException
	{
		return writeToFile( new ByteArrayInputStream( content.getBytes( "UTF-8" ) ), targetFile );
	}

	public static Observable<String> readTextFromFile( File file ) throws IOException
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
		if ( source.getPath().equals( target.getPath() ) )
		{
			return true;
		}

		if ( !forceOverwrite && target.exists() )
		{
			return false;
		}

		if ( !source.exists() || source.isDirectory() && target.exists() && !target.isDirectory() || !source.isDirectory() && target.isDirectory() )
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