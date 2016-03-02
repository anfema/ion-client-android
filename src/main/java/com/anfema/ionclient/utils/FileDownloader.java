package com.anfema.ionclient.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class FileDownloader
{
	public static void saveFileToLocation( String fileUrl, String filePath, ProgressCallback progressCallback ) throws IOException
	{
		// Connect to url
		File tempFile = new File( filePath );
		URL url = new URL( fileUrl );
		URLConnection connection = url.openConnection();
		connection.connect();

		// Get file length
		int length = connection.getContentLength();
		// input stream to read file - with 8k buffer
		InputStream inputStream = new BufferedInputStream( url.openStream(), 10 * 1024 );
		// Output stream to write file in SD card
		if ( !tempFile.exists() )
		{
			tempFile.createNewFile();
		}
		OutputStream outputStream = new FileOutputStream( filePath, false );
		byte data[] = new byte[ 1024 ];
		long total = 0;
		int count;
		while ( ( count = inputStream.read( data ) ) != -1 )
		{
			total += count;
			// Publish the progress which triggers onProgressUpdate method
			if ( progressCallback != null )
			{
				progressCallback.progress( ( float ) total / length );
			}

			// Write data to file
			outputStream.write( data, 0, count );
		}
		// Flush output
		outputStream.flush();
		// Close streams
		outputStream.close();
		inputStream.close();
	}

	public interface ProgressCallback
	{
		void progress( Float progress );
	}
}
