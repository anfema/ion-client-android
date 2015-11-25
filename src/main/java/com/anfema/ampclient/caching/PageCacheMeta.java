package com.anfema.ampclient.caching;

import android.content.Context;

import com.squareup.okhttp.HttpUrl;

import org.joda.time.DateTime;

public class PageCacheMeta extends CacheMeta
{
	private DateTime lastChanged;

	public PageCacheMeta( String filename, DateTime lastChanged )
	{
		super( filename );
		this.lastChanged = lastChanged;
	}

	/**
	 * Use MD5 of request URL as filename
	 */
	public PageCacheMeta( HttpUrl requestUrl, DateTime lastChanged )
	{
		super( requestUrl );
		this.lastChanged = lastChanged;
	}

	public DateTime getLastChanged()
	{
		return lastChanged;
	}

	public void setLastChanged( DateTime lastChanged )
	{
		this.lastChanged = lastChanged;
	}

	public boolean isOutdated( DateTime serverDate )
	{
		return lastChanged.isBefore( serverDate );
	}

	// ****** Store and retrieve from shared preferences ***********

	public static PageCacheMeta retrieve( String requestUrl, Context context )
	{
		return CacheMeta.retrieve( requestUrl, context, PageCacheMeta.class );
	}
}
