package com.anfema.ionclient.archive;

import android.content.Context;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.mediafiles.IonFiles;
import com.anfema.ionclient.pages.IonPages;

public class IonArchiveFactory
{
	public static IonArchive newInstance( IonPages ionPages, IonFiles ionFiles, IonConfig config, Context context )
	{
		return new IonArchiveDownloader( ionPages, ionFiles, config, context );
	}
}
