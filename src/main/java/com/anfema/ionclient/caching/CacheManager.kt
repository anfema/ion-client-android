package com.anfema.ionclient.caching

import android.content.Context
import com.anfema.ionclient.CollectionProperties
import com.anfema.ionclient.caching.index.CacheIndexStore
import com.anfema.ionclient.utils.FileUtils
import com.anfema.ionclient.utils.IonLog

/**
 * Provides cache clearing functionality
 */
object CacheManager {
    /**
     * clear entire cache - both memory and file cache
     */
    @JvmStatic
    fun clear(context: Context?) {
        IonLog.i("Cache Clear", "Clear entire cache")
        // clears cache index in both memory and file cache
        CacheIndexStore.clear(context!!)
        MemoryCache.clear()
        FileUtils.deleteRecursive(FilePaths.getFilesDir(context))
    }

    /**
     * clear entire memory cache - does not affect file cache
     */
    @JvmStatic
    fun clearMemoryCache() {
        IonLog.i("Cache Clear", "Clear memory cache")
        MemoryCache.clear()
    }

    /**
     * clear collection defined through [collectionProperties] entirely from cache
     * - only specific collection data is cleared from file cache
     * - memory cache is cleared entirely
     */
    @JvmStatic
    fun clearCollection(collectionProperties: CollectionProperties, context: Context?) {
        IonLog.i("Cache Clear", "Clear collection cache for " + collectionProperties.collectionIdentifier)
        CacheIndexStore.clearCollection(collectionProperties, context!!)
        MemoryCache.clear()
        FileUtils.deleteRecursive(FilePaths.getCollectionFolderPath(collectionProperties, context))
    }
}
