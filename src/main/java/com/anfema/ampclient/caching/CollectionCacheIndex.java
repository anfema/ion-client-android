package com.anfema.ampclient.caching;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.pages.PagesUrls;
import com.anfema.ampclient.utils.DateTimeUtils;
import com.anfema.ampclient.utils.Log;

import org.joda.time.DateTime;

import okhttp3.HttpUrl;

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

	public CollectionCacheIndex( String filename, DateTime lastUpdated, String lastModified )
	{
		super( filename );
		this.lastUpdated = lastUpdated;
		this.lastModified = lastModified;
	}

	/**
	 * Use MD5 of request URL as filename
	 */
	public CollectionCacheIndex( HttpUrl requestUrl, DateTime lastUpdated, String lastModified )
	{
		super( requestUrl );
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

	public boolean isOutdated( AmpConfig config )
	{
		return lastUpdated.isBefore( DateTimeUtils.now().minusMinutes( config.minutesUntilCollectionRefetch ) );
	}

	public DateTime getLastModifiedDate()
	{
		if ( lastModified == null )
		{
			Log.d( "Last Modified", "String is null" );
			return null;
		}
		try
		{
			DateTime lastModifiedDate = DateTimeUtils.parseDateTime( lastModified );
			Log.d( "Last Modified", "Successfully parsed " + lastModified );
			return lastModifiedDate;
		}
		catch ( IllegalArgumentException e )
		{
			Log.e( "Last Modified", "Parse error for: " + lastModified );
			Log.ex( "Last Modified", e );
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

	public static CollectionCacheIndex retrieve( String requestUrl, String collectionIdentifier, Context context )
	{
		return CacheIndexStore.retrieve( requestUrl, CollectionCacheIndex.class, collectionIdentifier, context );
	}

	public static CollectionCacheIndex retrieve( AmpConfig config, Context context )
	{
		String requestUrl = PagesUrls.getCollectionUrl( config );
		return CacheIndexStore.retrieve( requestUrl, CollectionCacheIndex.class, config.collectionIdentifier, context );
	}

	public static void save( AmpConfig config, Context context, String lastModified )
	{
		String url = PagesUrls.getCollectionUrl( config );
		CollectionCacheIndex cacheIndex = new CollectionCacheIndex( url, DateTimeUtils.now(), lastModified );
		CacheIndexStore.save( url, cacheIndex, config, context );
	}
}
