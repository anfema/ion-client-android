package com.anfema.ionclient.utils;


import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.util.Locale;

/**
 * These methods fully operate in UTC time zone.
 * {@link DateTime} objects are parsed with UTC and format and toString methods print dates assuming not local time zone but UTC.
 */
public class DateTimeUtils extends org.joda.time.DateTimeUtils
{
	public static final String DATETIME_PATTERN             = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String DATETIME_PATTERN_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	public static final String DATETIME_PATTERN_RFC_1123    = "EEE, dd MMM yyyy HH:mm:ss zzz";

	public static final DateTimeFormatter FORMATTER;
	public static final DateTimeFormatter FORMATTER_WITH_MILLIS;
	public static final DateTimeFormatter FORMATTER_RFC_1123 = DateTimeFormat.forPattern( DATETIME_PATTERN_RFC_1123 ).withZoneUTC().withLocale( Locale.US );

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
	 * Parse datetime strings by trying the two patterns Ion uses. As a third option try the RFC 1123 standard format.
	 *
	 * @param dateString string representation of a date
	 * @return parsed {@link DateTime} object
	 * @throws IllegalArgumentException if {@param dateString} is does not fit with the default datetime patterns of this class
	 * @throws NullPointerException     if {@param dateString} is null
	 */
	public static DateTime parseOrThrow( String dateString ) throws IllegalArgumentException, NullPointerException
	{
		try
		{
			return FORMATTER.parseDateTime( dateString );
		}
		catch ( IllegalArgumentException e )
		{
			try
			{
				return FORMATTER_WITH_MILLIS.parseDateTime( dateString );
			}
			catch ( IllegalArgumentException e2 )
			{
				return FORMATTER_RFC_1123.parseDateTime( dateString );
			}
		}
	}

	/**
	 * Parse datetime strings by trying the two patterns Ion uses. As a third option try the RFC 1123 standard format.
	 *
	 * @param dateString string representation of a date
	 * @return parsed {@link DateTime} object or null if parsing was not successful
	 */
	public static DateTime parseOrNull( String dateString )
	{
		try
		{
			return parseOrThrow( dateString );
		}
		catch ( IllegalArgumentException | NullPointerException e )
		{
			return null;
		}
	}

	/**
	 * Parse datetime strings by applying {@param inputPattern} and assuming timezone UTC.
	 *
	 * @param dateString   string representation of a date
	 * @param inputPattern e.g. "dd.MM.yy"
	 * @return parsed {@link DateTime} object
	 * @throws IllegalArgumentException if {@param dateString} is does not fit with the default datetime patterns of this class
	 * @throws NullPointerException     if {@param dateString} is null
	 */
	public static DateTime parseOrThrow( String dateString, String inputPattern ) throws IllegalArgumentException, NullPointerException
	{
		return DateTimeFormat.forPattern( inputPattern ).withZoneUTC().withLocale( Locale.US ).parseDateTime( dateString );
	}

	/**
	 * Parse datetime strings by applying {@param inputPattern} and assuming timezone UTC.
	 *
	 * @param dateString   string representation of a date
	 * @param inputPattern e.g. "dd.MM.yy"
	 * @return parsed {@link DateTime} object or null if parsing was not successful
	 */
	public static DateTime parseOrNull( String dateString, String inputPattern )
	{
		try
		{
			return parseOrThrow( dateString, inputPattern );
		}
		catch ( IllegalArgumentException | NullPointerException e )
		{
			return null;
		}
	}

	/**
	 * @param outPattern e.g. "dd.MM.yy"
	 * @return string representation of {@link DateTime} object or empty string if {@param dateTime} is not valid
	 */
	public static String format( DateTime dateTime, String outPattern )
	{
		if ( dateTime == null )
		{
			return "";
		}
		DateTimeFormatter dtf = new DateTimeFormatterBuilder().appendPattern( outPattern ).toFormatter().withZoneUTC();
		return dtf.print( dateTime );
	}

	/**
	 * Convert one string representation of a date to another
	 *
	 * @param inDateString initial string representation of a date to be converted to another string
	 * @param outPattern   e.g. "dd.MM.yy"
	 * @return string representation of {@link DateTime} object or empty string if {@param dateTime} is not valid
	 */
	public static String format( String inDateString, String outPattern )
	{
		DateTime dateDeserialized = parseOrNull( inDateString );
		return format( dateDeserialized, outPattern );
	}

	/**
	 * Return string representation according to {@link DateTimeUtils#DATETIME_PATTERN}
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
