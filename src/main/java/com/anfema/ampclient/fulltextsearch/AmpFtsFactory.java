package com.anfema.ampclient.fulltextsearch;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.mediafiles.AmpFiles;
import com.anfema.ampclient.pages.AmpPages;

public class AmpFtsFactory
{
	public static AmpFts newInstance( AmpPages ampPages, AmpFiles ampFiles, AmpConfig config, Context context )
	{
		return new AmpFtsImpl( ampPages, ampFiles, config, context );
	}
}
