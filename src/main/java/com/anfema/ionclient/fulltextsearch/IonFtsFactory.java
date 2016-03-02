package com.anfema.ionclient.fulltextsearch;

import android.content.Context;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.mediafiles.IonFiles;
import com.anfema.ionclient.pages.IonPages;

public class IonFtsFactory
{
	public static IonFts newInstance( IonPages ionPages, IonFiles ionFiles, IonConfig config, Context context )
	{
		return new IonFtsImpl( ionPages, ionFiles, config, context );
	}
}
