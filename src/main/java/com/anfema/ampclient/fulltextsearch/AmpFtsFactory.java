package com.anfema.ampclient.fulltextsearch;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.pages.AmpPages;

public class AmpFtsFactory
{
	public static AmpFts newInstance( AmpPages ampPages, AmpConfig config, Context context )
	{
		return new AmpFtsImpl( ampPages, config, context );
	}
}
