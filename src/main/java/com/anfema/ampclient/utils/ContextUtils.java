package com.anfema.ampclient.utils;

import android.content.Context;

public class ContextUtils
{
	public static Context getApplicationContext( Context context )
	{
		// get application context
		if ( context == null )
		{
			throw new IllegalArgumentException( "Context must not be null." );
		}
		return context.getApplicationContext();
	}
}
