package com.anfema.ampclient.utils;

import android.content.Context;

import com.anfema.ampclient.exceptions.ContextIsNullException;

public class ContextUtils
{
	public static Context getApplicationContext( Context context )
	{
		// get application context
		if ( context == null )
		{
			throw new ContextIsNullException();
		}
		return context.getApplicationContext();
	}
}
