package com.anfema.ionclient.caching;

import android.content.Context;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.caching.index.CacheIndexStore;
import com.anfema.ionclient.caching.index.MemoryCacheIndex;
import com.anfema.ionclient.utils.FileUtils;
import com.anfema.ionclient.utils.IonLog;

/**
 * Provides cache clearing functionality
 */
public class CacheManager
{
	/**
	 * clear entire cache - both memory and file cache
	 */
	public static void clear( Context context )
	{
		IonLog.i( "Cache Clear", "Clear entire cache" );
		// clears cache index in both memory and file cache
		CacheIndexStore.clear( context );
		MemoryCache.clear();
		FileUtils.deleteRecursive( FilePaths.getFilesDir( context ) );
	}

	/**
	 * clear entire memory cache - does not affect file cache
	 */
	public static void clearMemoryCache()
	{
		IonLog.i( "Cache Clear", "Clear memory cache" );
		MemoryCache.clear();
		MemoryCacheIndex.clear();
	}

	/**
	 * clear collection defined through {@param config} entirely from cache
	 * - only specific collection data is cleared from file cache
	 * - memory cache is cleared entirely
	 */
	public static void clearCollection( IonConfig config, Context context )
	{
		IonLog.i( "Cache Clear", "Clear collection cache for " + config.collectionIdentifier );
		CacheIndexStore.clearCollection( config, context );
		MemoryCache.clear();
		FileUtils.deleteRecursive( FilePaths.getCollectionFolderPath( config, context ) );
	}
}
