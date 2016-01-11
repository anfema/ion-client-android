package com.anfema.ampclient.caching;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.exceptions.NoAmpPagesRequestException;
import com.anfema.ampclient.pages.PagesUrls;
import com.anfema.ampclient.utils.DateTimeUtils;
import com.anfema.ampclient.utils.Log;
import com.squareup.okhttp.HttpUrl;

import org.joda.time.DateTime;

public class CollectionCacheIndex extends CacheIndex
{
	private DateTime lastUpdated;
	private DateTime lastChanged;

	public CollectionCacheIndex( String filename, DateTime lastUpdated, DateTime lastChanged )
	{
		super( filename );
		this.lastUpdated = lastUpdated;
		this.lastChanged = lastChanged;
	}

	/**
	 * Use MD5 of request URL as filename
	 */
	public CollectionCacheIndex( HttpUrl requestUrl, DateTime lastUpdated, DateTime lastChanged )
	{
		super( requestUrl );
		this.lastUpdated = lastUpdated;
		this.lastChanged = lastChanged;
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

	public DateTime getLastChanged()
	{
		return lastChanged;
	}

	public void setLastChanged( DateTime lastChanged )
	{
		this.lastChanged = lastChanged;
	}

	// Persistence - shared preferences

	public static CollectionCacheIndex retrieve( String requestUrl, String collectionIdentifier, Context context )
	{
		return CacheIndex.retrieve( requestUrl, CollectionCacheIndex.class, collectionIdentifier, context );
	}

	public static CollectionCacheIndex retrieve( AmpConfig config, Context context )
	{
		String requestUrl = PagesUrls.getCollectionUrl( config );
		return CacheIndex.retrieve( requestUrl, CollectionCacheIndex.class, config.collectionIdentifier, context );
	}

	public static void save( AmpConfig config, Context context, DateTime lastChanged )
	{
		String url = PagesUrls.getCollectionUrl( config );

		try
		{
			if ( FilePaths.getJsonFilePath( url, context ).exists() )
			{
				CollectionCacheIndex cacheMeta = new CollectionCacheIndex( url, DateTimeUtils.now(), lastChanged );
				save( url, cacheMeta, config.collectionIdentifier, context );
			}
		}
		catch ( NoAmpPagesRequestException e )
		{
			Log.e( "Cache Meta", "Could not save cache meta entry for " + url );
			Log.ex( e );
		}
	}
}
