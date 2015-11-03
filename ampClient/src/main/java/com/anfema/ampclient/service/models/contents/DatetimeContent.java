package com.anfema.ampclient.service.models.contents;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatetimeContent extends AContent
{
	public static final String           DATETIME_PATTERN   = "yyyy-MM-dd'T'HH:mm:ssZ";
	public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat( DATETIME_PATTERN, Locale.US );

	private String datetime;

	public String getDatetimeString()
	{
		return datetime;
	}

	public Date getDate() throws ParseException
	{
		return SIMPLE_DATE_FORMAT.parse( datetime );
	}
}
