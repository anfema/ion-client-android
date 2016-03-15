package com.anfema.ionclient.caching;

import android.util.LruCache;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.pages.IonPageUrls;
import com.anfema.ionclient.pages.models.Collection;
import com.anfema.ionclient.pages.models.Page;

public class MemoryCache
{
	// key: collection/page URL, value: either of type Collection or Page
	private static volatile LruCache<String, Object> collectionsPagesMemoryCache = new LruCache<>( IonConfig.pagesMemCacheSize );

	public static Collection getCollection( String collectionUrl )
	{
		return Collection.class.cast( collectionsPagesMemoryCache.get( collectionUrl ) );
	}

	public static Collection getCollection( IonConfig config )
	{
		String collectionUrl = IonPageUrls.getCollectionUrl( config );
		return getCollection( collectionUrl );
	}

	public static void saveCollection( Collection collection, String collectionUrl )
	{
		collectionsPagesMemoryCache.put( collectionUrl, collection );
	}

	public static void saveCollection( Collection collection, IonConfig config )
	{
		String collectionUrl = IonPageUrls.getCollectionUrl( config );
		saveCollection( collection, collectionUrl );
	}

	public static Page getPage( String pageUrl )
	{
		return Page.class.cast( collectionsPagesMemoryCache.get( pageUrl ) );
	}

	public static Page getPage( String pageIdentifier, IonConfig config )
	{
		String pageUrl = IonPageUrls.getPageUrl( config, pageIdentifier );
		return getPage( pageUrl );
	}

	/**
	 * Save page to memory cache
	 *
	 * @param page page which should be saved
	 */
	public static void savePage( Page page, IonConfig config )
	{
		String pageUrl = IonPageUrls.getPageUrl( config, page.identifier );
		collectionsPagesMemoryCache.put( pageUrl, page );
	}

	public static void clear()
	{
		collectionsPagesMemoryCache.evictAll();
	}
}
