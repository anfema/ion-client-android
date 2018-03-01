package com.anfema.ionclient.caching.index;

import android.content.Context;
import android.support.annotation.Nullable;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.pages.IonPageUrls;
import com.anfema.ionclient.utils.DateTimeUtils;
import com.anfema.ionclient.utils.IonLog;

import org.joda.time.DateTime;

public class CollectionCacheIndex extends CacheIndex
{
	/**
	 * Last time when collection server call has been made (even if the server response code is 304 NOT MODIFIED)
	 */
	private DateTime lastUpdated;

	/**
	 * Received with the collection server call as response header "Last-Modified"
	 */
	private String lastModified;

	public CollectionCacheIndex( DateTime lastUpdated, String lastModified )
	{
		this.lastUpdated = lastUpdated;
		this.lastModified = lastModified;
	}

	public DateTime getLastUpdated()
	{
		return lastUpdated;
	}

	public void setLastUpdated( DateTime lastUpdated )
	{
		this.lastUpdated = lastUpdated;
	}

	public boolean isOutdated( IonConfig config )
	{
		return lastUpdated.isBefore( DateTimeUtils.now().minusMinutes( config.minutesUntilCollectionRefetch ) );
	}

	public DateTime getLastModifiedDate()
	{
		if ( lastModified == null )
		{
			IonLog.d( "Last Modified", "String is null" );
			return null;
		}
		try
		{
			DateTime lastModifiedDate = DateTimeUtils.parseOrThrow( lastModified );
			IonLog.d( "Last Modified", "Successfully parsed " + lastModified );
			return lastModifiedDate;
		}
		catch ( IllegalArgumentException e )
		{
			IonLog.e( "Last Modified", "Parse error for: " + lastModified );
			IonLog.ex( "Last Modified", e );
			return null;
		}
	}

	public String getLastModified()
	{
		return lastModified;
	}

	public void setLastModified( String lastModified )
	{
		this.lastModified = lastModified;
	}

	// save & retrieve

	public static CollectionCacheIndex retrieve( IonConfig config, Context context )
	{
		String requestUrl = IonPageUrls.getCollectionUrl( config );
		return CacheIndexStore.retrieve( requestUrl, CollectionCacheIndex.class, config, context );
	}

	public static void save( IonConfig config, Context context, String lastModified, @Nullable DateTime lastUpdated )
	{
		String url = IonPageUrls.getCollectionUrl( config );
		if ( lastUpdated == null )
		{
			lastUpdated = DateTimeUtils.now();
		}
		CollectionCacheIndex cacheIndex = new CollectionCacheIndex( lastUpdated, lastModified );
		CacheIndexStore.save( url, cacheIndex, config, context );
	}
}
