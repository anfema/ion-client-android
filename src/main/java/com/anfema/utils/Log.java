package com.anfema.utils;

/**
 * Wraps Android's default logging class.
 * <p>
 * Additional features:
 * - specify a log level and a default tag:
 * This should be done in application's onCreate method by calling {@link #setup(int, String)} method.
 * You should always set log level to {@link #NONE} in release mode!
 * - easy logging of exceptions with {@link #ex(Throwable)} or {@link #ex(String, Throwable)}
 *
 * @deprecated Replace with Logger from androidkit or IonLog
 */
@Deprecated
public class Log
{
	public static final int VERBOSE = android.util.Log.VERBOSE;
	public static final int DEBUG   = android.util.Log.DEBUG;
	public static final int INFO    = android.util.Log.INFO;
	public static final int WARN    = android.util.Log.WARN;
	public static final int ERROR   = android.util.Log.ERROR;
	public static final int NONE    = Integer.MAX_VALUE;

	private static int    logLevel   = VERBOSE;
	private static String defaultTag = "";

	// prevent initialization
	private Log()
	{
	}

	@SuppressWarnings("deprecation")
	public static void setup( int logLevel, String defaultTag )
	{
		Log.logLevel = logLevel;
		Log.defaultTag = defaultTag;
	}

	public static void v( String tag, String message )
	{
		if ( logLevel == VERBOSE )
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
		if ( logLevel <= DEBUG && logLevel >= VERBOSE )
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
		if ( logLevel <= INFO && logLevel >= VERBOSE )
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
		if ( logLevel <= WARN && logLevel >= VERBOSE )
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
		if ( logLevel <= ERROR && logLevel >= VERBOSE )
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
		if ( logLevel <= ERROR && logLevel >= VERBOSE )
		{
			android.util.Log.e( tag, android.util.Log.getStackTraceString( exception ) );
		}
	}

	public static void ex( Throwable exception )
	{
		ex( defaultTag, exception );
	}
}
