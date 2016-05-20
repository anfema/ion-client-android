package com.anfema.ionclient.caching;

import android.content.Context;
import android.util.LruCache;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.pages.IonPageUrls;
import com.anfema.ionclient.pages.models.Collection;
import com.anfema.ionclient.pages.models.Page;
import com.anfema.ionclient.pages.models.SizeAware;
import com.anfema.ionclient.utils.MemoryUtils;

public class MemoryCache
{
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

	public static Collection getCollection( IonConfig config )
	{
		String collectionUrl = IonPageUrls.getCollectionUrl( config );
		return getCollection( collectionUrl );
	}

	public static void saveCollection( Collection collection, String collectionUrl, Context context )
	{
		ensureLruCacheInitialized( context );
		collectionsPagesMemoryCache.put( collectionUrl, collection );
	}

	public static void saveCollection( Collection collection, IonConfig config, Context context )
	{
		String collectionUrl = IonPageUrls.getCollectionUrl( config );
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
	public static void savePage( Page page, IonConfig config, Context context )
	{
		ensureLruCacheInitialized( context );
		String pageUrl = IonPageUrls.getPageUrl( config, page.identifier );
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

		if ( IonConfig.pagesMemCacheSize <= 0 )
		{
			// use default size (ca. 14 % of available memory cache)
			IonConfig.pagesMemCacheSize = calcDefaultPagesMemCacheSize( context );
		}

		// use 90 % of declared pages memory cache for actual content (and 10 % for index entries)
		int memCacheSize = IonConfig.pagesMemCacheSize * 9 / 10;
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
