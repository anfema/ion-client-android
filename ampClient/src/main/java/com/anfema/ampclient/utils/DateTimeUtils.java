package com.anfema.ampclient.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;

public class DateTimeUtils extends org.joda.time.DateTimeUtils
{
	public static final String            DATETIME_PATTERN             = "yyyy-MM-dd'T'HH:mm:ssZZ";
	public static final String            DATETIME_PATTERN_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
	public static final DateTimeFormatter FORMATTER                    = DateTimeFormat.forPattern( DATETIME_PATTERN );
	public static final DateTimeFormatter FORMATTER_WITH_MILLIS        = DateTimeFormat.forPattern( DATETIME_PATTERN_WITH_MILLIS );

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
}
