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
	private static final int MINUTES_UNTIL_COLLECTION_REFETCH = 5;

	private DateTime lastUpdated;

	public CollectionCacheIndex( String filename, DateTime lastUpdated )
	{
		super( filename );
		this.lastUpdated = lastUpdated;
	}

	/**
	 * Use MD5 of request URL as filename
	 */
	public CollectionCacheIndex( HttpUrl requestUrl, DateTime lastUpdated )
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

	public static CollectionCacheIndex retrieve( String requestUrl, String collectionIdentifier, Context context )
	{
		return CacheIndex.retrieve( requestUrl, CollectionCacheIndex.class, collectionIdentifier, context );
	}

	public static void save( AmpConfig config, Context context )
	{
		String url = PagesUrls.getCollectionUrl( config );

		try
		{
			if ( FilePaths.getJsonFilePath( url, context ).exists() )
			{
				CollectionCacheIndex cacheMeta = new CollectionCacheIndex( url, DateTimeUtils.now() );
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
