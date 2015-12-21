package com.anfema.ampclient.caching;

import android.content.Context;

import com.anfema.ampclient.utils.DateTimeUtils;
import com.squareup.okhttp.HttpUrl;

import org.joda.time.DateTime;

public class CollectionCacheMeta extends CacheMeta
{
	private static final int MINUTES_UNTIL_COLLECTION_REFETCH = 5;

	private DateTime lastUpdated;

	public CollectionCacheMeta( String filename, DateTime lastUpdated )
	{
		super( filename );
		this.lastUpdated = lastUpdated;
	}

	/**
	 * Use MD5 of request URL as filename
	 */
	public CollectionCacheMeta( HttpUrl requestUrl, DateTime lastUpdated )
	{
		super( requestUrl );
		this.lastUpdated = lastUpdated;
	}

	public DateTime getLastUpdated()
	{
		return lastUpdated;
	}

	public void setLastUpdated( DateTime lastUpdated )
	{
		this.lastUpdated = lastUpdated;
	}

	public boolean isOutdated()
	{
		return lastUpdated.isBefore( DateTimeUtils.now().minusMinutes( MINUTES_UNTIL_COLLECTION_REFETCH ) );
	}

	// Persistence - shared preferences

	public static CollectionCacheMeta retrieve( String requestUrl, Context context )
	{
		return CacheMeta.retrieve( requestUrl, context, CollectionCacheMeta.class );
	}
}
