package com.anfema.ampclient.caching;

import android.util.LruCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds cache index entries in memory cache.
 * <p/>
 * Entries can be save and retrieved by requestUrl.
 * <p/>
 * A separate map is held for each collection, allowing quickly clearing the memory cache of a specific collection.
 * Analogously to shared preferences store in {@link CacheIndex}.
 */
public class MemoryCacheIndex
{
	/**
	 * outer key: collection identifier
	 * inner key: request URL
	 */
	private static Map<String, LruCache<String, CacheIndex>> memoryCacheIndices = new HashMap<>();

	public static synchronized <T extends CacheIndex> T get( String requestUrl, String collectionIdentifier, Class<T> clazz )
	{
		// get memory cache for collection
		LruCache<String, CacheIndex> memoryCacheIndex = memoryCacheIndices.get( collectionIdentifier );

		if ( memoryCacheIndex == null )
		{
			return null;
		}

		return clazz.cast( memoryCacheIndex.get( requestUrl ) );
	}

	public static synchronized <T extends CacheIndex> void put( String requestUrl, String collectionIdentifier, T index )
	{
		// get memory cache for collection
		LruCache<String, CacheIndex> memoryCacheIndex = memoryCacheIndices.get( collectionIdentifier );
		if ( memoryCacheIndex == null )
		{
			// TODO cleaner way to get LRU cache size
			memoryCacheIndex = new LruCache<>( MemoryCache.pagesMemoryCacheSize );
			memoryCacheIndices.put( collectionIdentifier, memoryCacheIndex );
		}
		memoryCacheIndex.put( requestUrl, index );
	}

	/**
	 * Clear index memory cache for a specific collection
	 *
	 * @param collectionIdentifier to specify the collection
	 */
	public static synchronized void clear( String collectionIdentifier )
	{
		// get memory cache for collection
		LruCache<String, CacheIndex> memoryCacheIndex = memoryCacheIndices.get( collectionIdentifier );

		if ( memoryCacheIndex != null )
		{
			memoryCacheIndex.evictAll();
		}
	}

	/**
	 * Clear index memory cache for all collections.
	 */
	public static synchronized void clearAll()
	{
		memoryCacheIndices.clear();
	}
}
