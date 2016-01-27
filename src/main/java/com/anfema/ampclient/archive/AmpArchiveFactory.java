package com.anfema.ampclient.archive;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.mediafiles.AmpFiles;
import com.anfema.ampclient.pages.AmpPages;

public class AmpArchiveFactory
{
	public static AmpArchive newInstance( AmpPages ampPages, AmpFiles ampFiles, AmpConfig config, Context context )
	{
		return new AmpArchiveDownloader( ampPages, ampFiles, config, context );
	}
}
