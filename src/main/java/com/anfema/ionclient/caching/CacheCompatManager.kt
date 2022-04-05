package com.anfema.ionclient.caching

import android.content.Context
import com.anfema.ionclient.BuildConfig

object CacheCompatManager {
    private const val PREFS_MAJOR_VERSION = "prefs_major_version"
    private const val KEY_MAJOR_VERSION = "key_major_version"

    // check needs to be performed only once during an app session
    private var checkPerformed = false

    /**
     * Checks if the current client version is still compatible with persistent cache. If not, the cache is cleared.
     */
    @JvmStatic
    fun cleanUp(context: Context) {
        if (checkPerformed) {
            return
        }
        val prefs = context.getSharedPreferences(PREFS_MAJOR_VERSION, 0)

        val oldCacheCompatibility = prefs.getInt(KEY_MAJOR_VERSION, 0)
        val cacheCompatibility = BuildConfig.CACHE_COMPATIBILITY

        if (cacheCompatibility != oldCacheCompatibility) {
            // clear cache and update cache compatibility version on file storage
            CacheManager.clear(context)
            prefs.edit().putInt(KEY_MAJOR_VERSION, cacheCompatibility).apply()
        }

        checkPerformed = true
    }
}
