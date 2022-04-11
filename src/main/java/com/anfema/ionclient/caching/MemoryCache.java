package com.anfema.ionclient.caching;

import android.content.Context;
import android.util.LruCache;

import com.anfema.ionclient.CollectionProperties;
import com.anfema.ionclient.pages.IonPageUrls;
import com.anfema.ionclient.pages.models.Collection;
import com.anfema.ionclient.pages.models.Page;
import com.anfema.ionclient.pages.models.SizeAware;
import com.anfema.ionclient.utils.MemoryUtils;

public class MemoryCache
{
	public static final int CALC_REASONABLE_SIZE = -1;

	/**
	 * Size of LRU memory cache (for all client instances accumulated). The Unit is bytes.
	 * <p>
	 * Value must be overwritten before first ION request is made, otherwise it won't have any effect.
	 * It is recommended to set it as early as possible, e.g. in onCreate() of application (or first activity).
	 * <p>
	 * Be careful not to exceed the available RAM of the application. You might want to define the memory cache size as a fraction of the
	 * available space. Therefore, you can use {@link MemoryUtils#calculateAvailableMemCache(Context)}.
	 * <p>
	 * If not set to a positive value, default cache size will be used.
	 */
	public static int pagesMemCacheSize = CALC_REASONABLE_SIZE;

	// key: collection/page URL, value: either of type Collection or Page
	private static volatile LruCache<String, SizeAware> collectionsPagesMemoryCache;

	public static Collection getCollection( String collectionUrl )
	{
		if ( collectionsPagesMemoryCache == null )
		{
			return null;
		}
		return ( Collection ) collectionsPagesMemoryCache.get( collectionUrl );
	}

	public static Collection getCollection( CollectionProperties collectionProperties )
	{
		String collectionUrl = IonPageUrls.getCollectionUrl( collectionProperties );
		return getCollection( collectionUrl );
	}

	public static void saveCollection( Collection collection, String collectionUrl, Context context )
	{
		ensureLruCacheInitialized( context );
		collectionsPagesMemoryCache.put( collectionUrl, collection );
	}

	public static void saveCollection( Collection collection, CollectionProperties collectionProperties, Context context )
	{
		String collectionUrl = IonPageUrls.getCollectionUrl( collectionProperties );
		saveCollection( collection, collectionUrl, context );
	}

	public static Page getPage( String pageUrl )
	{
		if ( collectionsPagesMemoryCache == null )
		{
			return null;
		}
		return ( Page ) collectionsPagesMemoryCache.get( pageUrl );
	}

	public static Page getPage( String pageIdentifier, CollectionProperties collectionProperties )
	{
		String pageUrl = IonPageUrls.getPageUrl( collectionProperties, pageIdentifier );
		return getPage( pageUrl );
	}

	/**
	 * Save page to memory cache
	 *
	 * @param page page which should be saved
	 */
	public static void savePage( Page page, CollectionProperties collectionProperties, Context context )
	{
		ensureLruCacheInitialized( context );
		String pageUrl = IonPageUrls.getPageUrl( collectionProperties, page.identifier );
		collectionsPagesMemoryCache.put( pageUrl, page );
	}

	public static void clear()
	{
		if ( collectionsPagesMemoryCache != null )
		{
			collectionsPagesMemoryCache.evictAll();
		}
	}

	private static synchronized void ensureLruCacheInitialized( Context context )
	{
		if ( collectionsPagesMemoryCache != null )
		{
			return;
		}

		if ( pagesMemCacheSize <= 0 )
		{
			// use default size (ca. 14 % of available memory cache)
			pagesMemCacheSize = calcDefaultPagesMemCacheSize( context );
		}

		// use 90 % of declared pages memory cache for actual content (and 10 % for index entries)
		int memCacheSize = pagesMemCacheSize * 9 / 10;
		collectionsPagesMemoryCache = new LruCache<String, SizeAware>( memCacheSize )
		{
			@Override
			protected int sizeOf( String key, SizeAware value )
			{
				return ( int ) value.byteCont();
			}
		};
	}

	public static int calcDefaultPagesMemCacheSize( Context context )
	{
		return MemoryUtils.calculateAvailableMemCache( context ) / 7;
	}
}
