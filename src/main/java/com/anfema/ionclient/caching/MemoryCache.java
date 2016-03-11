package com.anfema.ionclient.caching;

import android.util.LruCache;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.pages.IonPageUrls;
import com.anfema.ionclient.pages.models.Collection;
import com.anfema.ionclient.pages.models.Page;

public class MemoryCache
{
	///  current collection
	private Collection collection;

	// key: page URL
	private LruCache<String, Page> pagesMemCache;

	public MemoryCache()
	{
		collection = null;
		pagesMemCache = new LruCache<>( IonConfig.pagesMemCacheSize );
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

	public Page getPage( String pageIdentifier, IonConfig config )
	{
		String pageUrl = IonPageUrls.getPageUrl( config, pageIdentifier );
		return getPage( pageUrl );
	}

	/**
	 * Save page to memory cache
	 *
	 * @param page page which should be saved
	 * @return the previous page mapped to the URL
	 */
	public Page savePage( Page page, IonConfig config )
	{
		String pageUrl = IonPageUrls.getPageUrl( config, page.identifier );
		return pagesMemCache.put( pageUrl, page );
	}

	public void clearPagesMemCache()
	{
		pagesMemCache.evictAll();
	}
}
