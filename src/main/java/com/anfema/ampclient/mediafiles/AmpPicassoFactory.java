package com.anfema.ampclient.mediafiles;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;

public class AmpPicassoFactory
{
	public static AmpPicasso newInstance( AmpFiles ampFiles, AmpConfig config, Context context )
	{
		return new AmpPicassoWithCaching( ampFiles, config, context );
	}
}
