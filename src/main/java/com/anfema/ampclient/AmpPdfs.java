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

	public static void showPdf( File file, Context context )
	{
		showPdf( file, file.getPath(), context );
	}

	/**
	 * PDF file from internal storage is passed to an external app via an intent. In order to enable the PDF reader app to
	 *
	 * @param file
	 * @param logInfo
	 * @param context
	 */
	public static void showPdf( File file, String logInfo, Context context )
	{
		try
		{
			Intent intent = new Intent( Intent.ACTION_VIEW );
			Uri contentUri = FileProvider.getUriForFile( context, context.getString( R.string.file_provider_authority ), file );
			intent.setDataAndType( contentUri, "application/pdf" );
			context.startActivity( intent );
			Log.i( "PDF Intent", "opening PDF " + logInfo );
		}
		catch ( ActivityNotFoundException e )
		{
			Toast.makeText(context, R.string.pdf_display_error, Toast.LENGTH_SHORT).show();
			Log.ex( e );
		}
	}

	/**
	 * This method is only for experimental/debugging purposes. This approach only works if PDF file is NOT in internal storage.
	 *
	 * @param file
	 * @param context
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
