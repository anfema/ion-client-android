package com.anfema.ampclient.utils;

import android.content.Context;

import com.anfema.ampclient.exceptions.ContextNullPointerException;

public class ContextUtils
{
	public static Context getApplicationContext( Context context )
	{
		// get application context
		if ( context == null )
		{
			throw new ContextNullPointerException();
		}
		return context.getApplicationContext();
	}
}
