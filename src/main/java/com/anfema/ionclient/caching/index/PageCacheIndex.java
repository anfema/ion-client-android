package com.anfema.ionclient.caching.index;

import android.content.Context;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.pages.IonPageUrls;
import com.anfema.ionclient.pages.models.Page;

import org.joda.time.DateTime;

public class PageCacheIndex extends CacheIndex
{
	/**
	 * Last changed info received from server in the page response
	 */
	private DateTime lastChanged;

	public PageCacheIndex( DateTime lastChanged )
	{
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

	public static PageCacheIndex retrieve( String requestUrl, IonConfig config, Context context )
	{
		return CacheIndexStore.retrieve( requestUrl, PageCacheIndex.class, config, context );
	}

	public static void save( Page page, IonConfig config, Context context )
	{
		save( page.identifier, page.last_changed, config, context );
	}

	public static void save( String pageIdentifier, DateTime pageLastChanged, IonConfig config, Context context )
	{
		String url = IonPageUrls.getPageUrl( config, pageIdentifier );
		PageCacheIndex cacheIndex = new PageCacheIndex( pageLastChanged );
		CacheIndexStore.save( url, cacheIndex, config, context );
	}
}
