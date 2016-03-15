package com.anfema.ionclient.caching.index;

import android.util.LruCache;

import com.anfema.ionclient.IonConfig;

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
	private static volatile LruCache<String, CacheIndex> memoryCacheIndices = new LruCache<>( IonConfig.pagesMemCacheSize );

	public static <T extends CacheIndex> T get( String requestUrl, Class<T> clazz )
	{
		return clazz.cast( memoryCacheIndices.get( requestUrl ) );
	}

	public static <T extends CacheIndex> void put( String requestUrl, T index )
	{
		memoryCacheIndices.put( requestUrl, index );
	}

	/**
	 * Clear the entire index memory cache
	 */
	public static synchronized void clear()
	{
		memoryCacheIndices.evictAll();
	}
}
