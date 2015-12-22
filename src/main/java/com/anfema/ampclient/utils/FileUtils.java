package com.anfema.ampclient.utils;

import android.support.annotation.NonNull;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import rx.Observable;

public class FileUtils
{
	// TODO synchronize operations on file paths
	public static volatile Set<String> ioLocks = new HashSet<>();

	public static final String SLASH = "/";

	public static File writeToFile( InputStream inputStream, File file ) throws IOException
	{
		Log.d( "FileUtil", "write to file: " + file.getPath() );

		File fileTemp = createTempFile( file );

		OutputStream outputStream = new BufferedOutputStream( new FileOutputStream( fileTemp ) );
		IOUtils.copy( inputStream, outputStream );
		outputStream.close();

		boolean writeSuccess = move( fileTemp, file, true );
		return writeSuccess ? file : null;
	}

	/**
	 * Helper function to write content String to a file.
	 * <p>
	 * Should not be called from main thread.
	 *
	 * @param content content String to save
	 * @param file    File to save object(s) to
	 */
	public static File writeTextToFile( String content, File file ) throws IOException
	{
		Log.d( "FileUtil", "write to file: " + file.getPath() );

		File fileTemp = createTempFile( file );

		OutputStream outputStream = new FileOutputStream( fileTemp, false );
		BufferedWriter bufferedWriter = new BufferedWriter( new OutputStreamWriter( outputStream ) );
		bufferedWriter.write( content );
		bufferedWriter.close();

		boolean writeSuccess = move( fileTemp, file, true );
		return writeSuccess ? file : null;
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
		} ).compose( RxUtils.runOnIoThread() );
	}

	public static Observable<File> unTar( final File inputFile, final File outputDir )
	{
		return Observable.just( null )
				.flatMap( o -> {
					try
					{
						return Observable.from( performUnTar( inputFile, outputDir ) );
					}
					catch ( IOException e )
					{
						return Observable.error( e );
					}
					catch ( ArchiveException e )
					{
						return Observable.error( e );
					}
				} )
				.compose( RxUtils.runOnComputionThread() );
	}

	/**
	 * Untar an input file into an output file.
	 * <p>
	 * The output file is created in the output folder, having the same name
	 * as the input file, minus the '.tar' extension.
	 *
	 * @param inputFile the input .tar file
	 * @param outputDir the output directory file.
	 * @return The {@link List} of {@link File}s with the untared content.
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ArchiveException
	 */
	private static List<File> performUnTar( final File inputFile, final File outputDir ) throws FileNotFoundException, IOException, ArchiveException
	{
		final String TAG = "Un-Tar";

		Log.d( TAG, String.format( "Untaring %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath() ) );

		final List<File> untaredFiles = new LinkedList<>();
		final InputStream is = new FileInputStream( inputFile );
		final TarArchiveInputStream debInputStream = ( TarArchiveInputStream ) new ArchiveStreamFactory().createArchiveInputStream( "tar", is );
		TarArchiveEntry entry;
		while ( ( entry = ( TarArchiveEntry ) debInputStream.getNextEntry() ) != null )
		{
			final File outputFile = new File( outputDir, entry.getName() );
			if ( entry.isDirectory() )
			{
				Log.d( TAG, String.format( "Attempting to write output directory %s.", outputFile.getAbsolutePath() ) );
				if ( !createDir( outputFile ) )
				{
					throw new IllegalStateException( String.format( "Couldn't create directory %s.", outputFile.getAbsolutePath() ) );
				}
			}
			else
			{
				Log.d( String.format( "Creating output file %s.", outputFile.getAbsolutePath() ) );
				if ( !createDir( outputFile.getParentFile() ) )
				{
					throw new IllegalStateException( String.format( "Couldn't create directory %s.", outputFile.getParentFile().getAbsolutePath() ) );
				}


				File targetFile = writeToFile( debInputStream, outputFile );

				if ( targetFile != null )
				{
					untaredFiles.add( targetFile );
				}
			}
		}
		debInputStream.close();

		return untaredFiles;
	}

	public static boolean move( File source, File target, boolean forceOverwrite )
	{
		if ( !forceOverwrite && target.exists() )
		{
			return false;
		}

		reset( target );
		return source.renameTo( target );
	}

	/**
	 * Reset file if exists and create directory path so it is ready for i/o operations.
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
		Log.d( "FileUtil", "create dirs for: " + dir );
		return dir.mkdirs();
	}

	@NonNull
	public static File createTempFile( File file )
	{
		File fileTemp = new File( file.getPath() + ".temp" );
		reset( fileTemp );
		return fileTemp;
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