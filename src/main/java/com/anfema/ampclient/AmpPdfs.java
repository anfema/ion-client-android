package com.anfema.ampclient;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.anfema.ampclient.utils.Log;

import java.io.File;

public class AmpPdfs
{
	/**
	 * Start a PDF intent for a file, which is in internal storage, to pass to a PDF viewer app.
	 *
	 * @param pdfFile PDF file in internal storage
	 */
	public static void showPdf( File pdfFile, Context context )
	{
		showPdf( pdfFile, pdfFile.getPath(), context );
	}

	/**
	 * Start a PDF intent for a file, which is in internal storage, to pass to a PDF viewer app.
	 *
	 * @param pdfFile PDF file in internal storage
	 * @param logInfo info about PDF which is logged when intent is started
	 */
	public static void showPdf( File pdfFile, String logInfo, Context context )
	{
		try
		{
			Intent intent = new Intent( Intent.ACTION_VIEW );
			Uri contentUri = FileProvider.getUriForFile( context, context.getString( R.string.file_provider_authority ), pdfFile );
			intent.setDataAndType( contentUri, "application/pdf" );
			intent.setFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION );
			context.startActivity( intent );
			Log.i( "PDF Intent", "opening PDF " + logInfo );
		}
		catch ( ActivityNotFoundException e )
		{
			Toast.makeText( context, R.string.pdf_display_error, Toast.LENGTH_SHORT ).show();
			Log.ex( e );
		}
	}

	/**
	 * Start a PDF intent for a file, which is in external storage, to pass to a PDF viewer app.
	 *
	 * @param file PDF file in internal storage
	 */
	public static void showPdfExt( File file, Context context )
	{
		Intent intent = new Intent();
		intent.setAction( Intent.ACTION_VIEW );
		Uri uri = Uri.fromFile( file );
		intent.setDataAndType( uri, "application/pdf" );
		context.startActivity( intent );
	}
}
