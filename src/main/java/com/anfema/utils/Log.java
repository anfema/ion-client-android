package com.anfema.utils;

public class Log
{
	public static int    logLevel   = android.util.Log.VERBOSE;
	public static String defaultTag = "";

	public static void v( String tag, String message )
	{
		if ( logLevel == android.util.Log.VERBOSE )
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
		if ( logLevel <= android.util.Log.DEBUG && logLevel >= android.util.Log.VERBOSE )
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
		if ( logLevel <= android.util.Log.INFO && logLevel >= android.util.Log.VERBOSE )
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
		if ( logLevel <= android.util.Log.WARN && logLevel >= android.util.Log.VERBOSE )
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
		if ( logLevel <= android.util.Log.ERROR && logLevel >= android.util.Log.VERBOSE )
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
		if ( logLevel <= android.util.Log.ERROR && logLevel >= android.util.Log.VERBOSE )
		{
			android.util.Log.e( tag, android.util.Log.getStackTraceString( exception ) );
		}
	}

	public static void ex( Throwable exception )
	{
		ex( defaultTag, exception );
	}
}
