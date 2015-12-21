package com.anfema.ampclient.archive;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.pages.AmpPages;

public class AmpArchiveFactory
{
	public static AmpArchive newInstance( AmpPages ampPages, AmpConfig config, Context context )
	{
		return new AmpArchiveDownloader( ampPages, config, context );
	}
}
