package com.anfema.ampclient.utils;

import com.squareup.okhttp.Response;

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
				} );
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
				if ( !createFolders( outputFile ) )
				{
					throw new IllegalStateException( String.format( "Couldn't create directory %s.", outputFile.getAbsolutePath() ) );
				}
			}
			else
			{
				File outputFileTemp = new File( outputFile.getPath() + ".temp" );
				Log.d( String.format( "Creating output file %s.", outputFile.getAbsolutePath() ) );
				if ( !createFolders( outputFile.getParentFile() ) )
				{
					throw new IllegalStateException( String.format( "Couldn't create directory %s.", outputFile.getParentFile().getAbsolutePath() ) );
				}

				final OutputStream outputFileStream = new FileOutputStream( outputFile );
				IOUtils.copy( debInputStream, outputFileStream );
				outputFileStream.close();
				if ( outputFile.exists() )
				{
					outputFile.delete();
				}
				boolean writeSuccess = outputFileTemp.renameTo( outputFile );
				if ( writeSuccess )
				{
					untaredFiles.add( outputFile );
				}
			}
		}
		debInputStream.close();

		return untaredFiles;
	}
}