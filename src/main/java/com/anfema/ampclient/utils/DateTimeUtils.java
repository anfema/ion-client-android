package com.anfema.ampclient.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.text.ParseException;
import java.util.TimeZone;

public class DateTimeUtils extends org.joda.time.DateTimeUtils
{
	public static final String DATETIME_PATTERN             = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String DATETIME_PATTERN_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	public static final DateTimeFormatter FORMATTER;
	public static final DateTimeFormatter FORMATTER_WITH_MILLIS;

	static
	{
		FORMATTER = formatterUtc( new DateTimeFormatterBuilder().appendPattern( DATETIME_PATTERN ) );
		FORMATTER_WITH_MILLIS = formatterUtc( new DateTimeFormatterBuilder().appendPattern( DATETIME_PATTERN_WITH_MILLIS ) );
	}

	private static DateTimeFormatter formatterUtc( DateTimeFormatterBuilder dateTimeFormatterBuilder )
	{
		return dateTimeFormatterBuilder
				.appendTimeZoneOffset( "Z", true, 2, 4 )
				.toFormatter()
				.withZoneUTC();
	}

	/**
	 * Parse datetime strings by trying the two patterns Amp uses.
	 */
	public static DateTime parseDateTime( String dateString ) throws ParseException
	{
		try
		{
			return FORMATTER.parseDateTime( dateString );
		}
		catch ( IllegalArgumentException e )
		{
			return FORMATTER_WITH_MILLIS.parseDateTime( dateString );
		}
	}

	/**
	 * Return string respresentatin according to {@link DateTimeUtils#DATETIME_PATTERN}
	 */
	public static String toString( DateTime dateTime )
	{
		return FORMATTER.print( dateTime );
	}

	/**
	 * Use this now method instead of {@link DateTime#now()} ensuring that timezone is UTC and milliseconds
	 * to allow isEqual comparisons with DateTime objects originating from server.
	 */
	public static DateTime now()
	{
		return DateTime.now().withMillisOfSecond( 0 ).withZone( DateTimeZone.UTC );
	}
}
