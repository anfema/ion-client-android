package com.anfema.ampclient.caching;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.exceptions.NoAmpPagesRequestException;
import com.anfema.ampclient.models.Page;
import com.anfema.ampclient.pages.PagesUrls;
import com.anfema.ampclient.utils.Log;
import com.squareup.okhttp.HttpUrl;

import org.joda.time.DateTime;

public class PageCacheIndex extends CacheIndex
{
	private DateTime lastChanged;

	public PageCacheIndex( String filename, DateTime lastChanged )
	{
		super( filename );
		this.lastChanged = lastChanged;
	}

	/**
	 * Use MD5 of request URL as filename
	 */
	public PageCacheIndex( HttpUrl requestUrl, DateTime lastChanged )
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

	// Persistence - shared preferences

	public static PageCacheIndex retrieve( String requestUrl, String collectionIdentifier, Context context )
	{
		return CacheIndex.retrieve( requestUrl, PageCacheIndex.class, collectionIdentifier, context );
	}

	public static void save( Page page, AmpConfig config, Context context )
	{
		save( page.identifier, page.last_changed, config, context );
	}

	public static void save( String pageIdentifier, DateTime pageLastChanged, AmpConfig config, Context context )
	{
		String url = PagesUrls.getPageUrl( config, pageIdentifier );
		try
		{
			if ( FilePaths.getJsonFilePath( url, context ).exists() )
			{
				PageCacheIndex cacheMeta = new PageCacheIndex( url, pageLastChanged );
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
