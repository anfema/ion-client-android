package com.anfema.ionclient.caching.index;

import android.content.Context;
import android.util.LruCache;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.caching.MemoryCache;

/**
 * Holds cache index entries in memory cache.
 * <p>
 * Entries can be save and retrieved by requestUrl.
 * <p>
 * Analogously to shared preferences store in {@link CacheIndex}.
 */
public class MemoryCacheIndex
{
	/**
	 * key: request URL
	 */
	private static volatile LruCache<String, CacheIndex> memoryCacheIndices;

	public static <T extends CacheIndex> T get( String requestUrl, Class<T> clazz )
	{
		if ( memoryCacheIndices == null )
		{
			return null;
		}
		return clazz.cast( memoryCacheIndices.get( requestUrl ) );
	}

	public static <T extends CacheIndex> void put( String requestUrl, T index, Context context )
	{
		ensureLruCacheInitialized( context );
		memoryCacheIndices.put( requestUrl, index );
	}

	/**
	 * Clear the entire index memory cache
	 */
	public static synchronized void clear()
	{
		if ( memoryCacheIndices != null )
		{
			memoryCacheIndices.evictAll();
		}
	}

	private static synchronized void ensureLruCacheInitialized( Context context )
	{
		if ( memoryCacheIndices != null )
		{
			return;
		}

		if ( IonConfig.pagesMemCacheSize <= 0 )
		{
			// use default size (ca. 14 % of available memory cache)
			IonConfig.pagesMemCacheSize = MemoryCache.calcDefaultPagesMemCacheSize( context );
		}

		// use 10 % of declared pages memory cache for index entries (and 90 % for actual content)
		int memCacheSize = IonConfig.pagesMemCacheSize / 10;
		memoryCacheIndices = new LruCache<String, CacheIndex>( memCacheSize )
		{
			@Override
			protected int sizeOf( String key, CacheIndex value )
			{
				return value.byteCount;
			}
		};
	}
}
