package com.anfema.ionclient.caching;

import android.content.Context;
import android.content.SharedPreferences;

import com.anfema.ionclient.BuildConfig;

public class CacheCompatManager
{
	private static final String PREFS_MAJOR_VERSION = "prefs_major_version";
	private static final String KEY_MAJOR_VERSION   = "key_major_version";

	// check needs to be performed only once during an app session
	private static boolean checkPerformed = false;

	/**
	 * Checks if the current client version is still compatible with persistent cache. If not, the cache is cleared.
	 */
	public static void cleanUp( Context context )
	{
		if ( checkPerformed )
		{
			return;
		}
		SharedPreferences prefs = context.getSharedPreferences( PREFS_MAJOR_VERSION, 0 );
		int oldCacheCompatibility = prefs.getInt( KEY_MAJOR_VERSION, 0 );
		int cacheCompatibility = BuildConfig.CACHE_COMPATIBILITY;
		if ( cacheCompatibility != oldCacheCompatibility )
		{
			// clear cache and update cache compatibility version on file storage
			CacheManager.clear( context );
			prefs.edit().putInt( KEY_MAJOR_VERSION, cacheCompatibility ).apply();
		}
		checkPerformed = true;
	}
}
