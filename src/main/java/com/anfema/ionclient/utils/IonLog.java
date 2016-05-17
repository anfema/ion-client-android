package com.anfema.ionclient.utils;

import com.anfema.ionclient.IonConfig;

public class IonLog
{
	public static final int VERBOSE = android.util.Log.VERBOSE;
	public static final int DEBUG   = android.util.Log.DEBUG;
	public static final int INFO    = android.util.Log.INFO;
	public static final int WARN    = android.util.Log.WARN;
	public static final int ERROR   = android.util.Log.ERROR;
	public static final int NONE    = Integer.MAX_VALUE;

	private static String defaultTag = "IonClient";

	// prevent initialization
	private IonLog()
	{
	}

	public static void v( String tag, String message )
	{
		if ( IonConfig.logLevel == VERBOSE )
		{
			android.util.Log.v( tag, message );
		}
	}

	public static void v( String message )
	{
		v( defaultTag, message );
	}

	public static void d( String tag, String message )
	{
		if ( IonConfig.logLevel <= DEBUG && IonConfig.logLevel >= VERBOSE )
		{
			android.util.Log.d( tag, message );
		}
	}

	public static void d( String message )
	{
		d( defaultTag, message );
	}

	public static void i( String tag, String message )
	{
		if ( IonConfig.logLevel <= INFO && IonConfig.logLevel >= VERBOSE )
		{
			android.util.Log.i( tag, message );
		}
	}

	public static void i( String message )
	{
		i( defaultTag, message );
	}

	public static void w( String tag, String message )
	{
		if ( IonConfig.logLevel <= WARN && IonConfig.logLevel >= VERBOSE )
		{
			android.util.Log.w( tag, message );
		}
	}

	public static void w( String message )
	{
		w( defaultTag, message );
	}

	public static void e( String tag, String message )
	{
		if ( IonConfig.logLevel <= ERROR && IonConfig.logLevel >= VERBOSE )
		{
			android.util.Log.e( tag, message );
		}
	}

	public static void e( String message )
	{
		e( defaultTag, message );
	}

	public static void ex( String tag, Throwable exception )
	{
		if ( IonConfig.logLevel <= ERROR && IonConfig.logLevel >= VERBOSE )
		{
			android.util.Log.e( tag, android.util.Log.getStackTraceString( exception ) );
		}
	}

	public static void ex( Throwable exception )
	{
		ex( defaultTag, exception );
	}
}
