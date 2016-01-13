package com.anfema.ampclient.caching;

import android.util.LruCache;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.pages.PagesUrls;
import com.anfema.ampclient.pages.models.Collection;
import com.anfema.ampclient.pages.models.Page;

public class MemoryCache
{
	///  current collection
	private Collection collection;

	// key: page URL
	private LruCache<String, Page> pagesMemCache;

	public MemoryCache()
	{
		// keeps 100 pages max
		collection = null;
		pagesMemCache = new LruCache<>( 100 );
	}

	public Collection getCollection()
	{
		return collection;
	}

	public void setCollection( Collection collection )
	{
		this.collection = collection;
	}

	public Page getPage( String pageUrl )
	{
		return pagesMemCache.get( pageUrl );
	}

	public Page getPage( String pageIdentifier, AmpConfig config )
	{
		String pageUrl = PagesUrls.getPageUrl( config, pageIdentifier );
		return getPage( pageUrl );
	}

	/**
	 * Save page to memory cache
	 *
	 * @param page page which should be saved
	 * @return the previous page mapped to the URL
	 */
	public Page savePage( Page page, AmpConfig config )
	{
		String pageUrl = PagesUrls.getPageUrl( config, page.identifier );
		return pagesMemCache.put( pageUrl, page );
	}

	public void clearPagesMemCache()
	{
		pagesMemCache.evictAll();
	}
}
