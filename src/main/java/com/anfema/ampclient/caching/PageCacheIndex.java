package com.anfema.ampclient.caching;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.pages.PagesUrls;
import com.anfema.ampclient.pages.models.Page;

import org.joda.time.DateTime;

import okhttp3.HttpUrl;

public class PageCacheIndex extends CacheIndex
{
	/**
	 * Last changed info received from server in the page response
	 */
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

	// save & retrieve

	public static PageCacheIndex retrieve( String requestUrl, String collectionIdentifier, Context context )
	{
		return CacheIndexStore.retrieve( requestUrl, PageCacheIndex.class, collectionIdentifier, context );
	}

	public static void save( Page page, AmpConfig config, Context context )
	{
		save( page.identifier, page.last_changed, config, context );
	}

	public static void save( String pageIdentifier, DateTime pageLastChanged, AmpConfig config, Context context )
	{
		String url = PagesUrls.getPageUrl( config, pageIdentifier );
		PageCacheIndex cacheIndex = new PageCacheIndex( url, pageLastChanged );
		CacheIndexStore.save( url, cacheIndex, config, context );
	}
}
