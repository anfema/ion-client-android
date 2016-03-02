package com.anfema.ionclient.utils;

import android.content.Context;

import com.anfema.ionclient.exceptions.ContextIsNullException;

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
