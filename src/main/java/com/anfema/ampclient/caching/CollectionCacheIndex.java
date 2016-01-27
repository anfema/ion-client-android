package com.anfema.ampclient.caching;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.pages.PagesUrls;
import com.anfema.ampclient.utils.DateTimeUtils;

import org.joda.time.DateTime;

import okhttp3.HttpUrl;

public class CollectionCacheIndex extends CacheIndex
{
	private DateTime lastUpdated;
	private String   lastModified;

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
