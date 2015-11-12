package com.anfema.ampclient.utils;

public class Log
{
	/**
	 * change setting via {@link Config#LOGCAT_LOGGING}
	 */
	static final boolean LOG = Config.loggingEnabled();

	public static String DEFAULT_TAG = "AmpClient";

	public static void v( String tag, String message )
	{
		if ( LOG )
		{
			android.util.Log.v( tag, message );
		}
	}

	public static void v( String message )
	{
		v( DEFAULT_TAG, message );
	}

	public static void d( String tag, String message )
	{
		if ( LOG )
		{
			android.util.Log.d( tag, message );
		}
	}

	public static void d( String message )
	{
		d( DEFAULT_TAG, message );
	}

	public static void i( String tag, String message )
	{
		if ( LOG )
		{
			android.util.Log.i( tag, message );
		}
	}

	public static void i( String message )
	{
		i( DEFAULT_TAG, message );
	}

	public static void w( String tag, String message )
	{
		if ( LOG )
		{
			android.util.Log.w( tag, message );
		}
	}

	public static void w( String message )
	{
		w( DEFAULT_TAG, message );
	}

	public static void e( String tag, String message )
	{
		if ( LOG )
		{
			android.util.Log.e( tag, message );
		}
	}

	public static void e( String message )
	{
		e( DEFAULT_TAG, message );
	}

	public static void ex( String tag, Throwable exception )
	{
		if ( LOG )
		{
			android.util.Log.e( tag, android.util.Log.getStackTraceString( exception ) );
		}
	}

	public static void ex( Throwable exception )
	{
		ex( DEFAULT_TAG, exception );
	}
}
