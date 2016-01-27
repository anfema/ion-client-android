package com.anfema.ampclient.mediafiles;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;

public class AmpFilesFactory
{
	public static AmpFiles newInstance( AmpConfig config, Context context )
	{
		return new AmpFilesWitchCaching( config, context );
	}
}
