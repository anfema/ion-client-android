package com.anfema.ionclient.caching.index

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.anfema.ionclient.CollectionProperties
import com.anfema.ionclient.caching.FilePaths
import com.anfema.ionclient.exceptions.NoIonPagesRequestException
import com.anfema.ionclient.serialization.GsonHolder
import com.anfema.ionclient.utils.IonLog
import com.anfema.utils.byteCount

/**
 * This is the global caching index table.
 * Single entries (either for collections, pages, or files) are stored in shared preferences.
 * There is a separate shared preferences instance for each collection.
 * An entry in shared preferences consist of the key, which is the URL the file has been requested from,
 * and the value, which is a subclass of [CacheIndex].
 */
object CacheIndexStore {
    internal inline fun <reified T : CacheIndex> retrieve(
        requestUrl: String,
        collectionProperties: CollectionProperties,
        context: Context,
    ): T? {
        // check shared preferences
        IonLog.d("Index Lookup", "$requestUrl from shared preferences")
        val prefs = getPrefs(collectionProperties, context)
        val json = prefs.getString(requestUrl, null)
        val index = GsonHolder.defaultInstance.fromJson(json, T::class.java)
        if (index != null) {
            // make cache index aware of its size by storing byte count to its field
            index.byteCount = json?.byteCount()?.toInt() ?: 0
        }
        return index
    }

    fun <T : CacheIndex> save(
        requestUrl: String,
        cacheIndex: T,
        collectionProperties: CollectionProperties,
        context: Context,
    ) {
        IonLog.d("Cache Index", "saving index for $requestUrl")
        try {
            val file = FilePaths.getFilePath(requestUrl, collectionProperties, context)
            if (file.exists() && file.length() > 0) {
                // make cache index aware of its size by storing byte count to its field
                val indexSerialized = GsonHolder.defaultInstance.toJson(cacheIndex)
                cacheIndex.byteCount = indexSerialized.byteCount().toInt()

                // save to shared preferences
                getPrefs(collectionProperties, context)
                    .edit()
                    .putString(requestUrl, indexSerialized)
                    .apply()

                // register shared prefs instance
                getMetaPrefs(context)
                    .edit()
                    .putBoolean(getPrefKey(collectionProperties), true)
                    .apply()
            } else {
                IonLog.e("Cache Index",
                    "Could not save cache index entry for $requestUrl\nBecause file does not exist.")
            }
        } catch (e: NoIonPagesRequestException) {
            IonLog.e("Cache Index", "Could not save cache index entry for $requestUrl")
            IonLog.ex(e)
        }
    }

    @JvmStatic
    fun delete(requestUrl: String, collectionProperties: CollectionProperties, context: Context) {
        IonLog.d("Cache Index", "deleting index for $requestUrl")

        // delete from shared preferences
        getPrefs(collectionProperties, context)
            .edit()
            .remove(requestUrl)
            .apply()
    }

    /**
     * @param collectionProperties to determine collection
     * @return all index entry URLs of collection
     */
    @JvmStatic
    fun retrieveAllUrls(collectionProperties: CollectionProperties, context: Context): Set<String> {
        // check shared preferences
        IonLog.d("Cache Index", "Retrieve all index entries of collection " + collectionProperties.collectionIdentifier)
        val prefs = getPrefs(collectionProperties, context)
        val urls: MutableSet<String> = HashSet()
        for ((key) in prefs.all) {
            urls.add(key)
        }
        return urls
    }

    /**
     * clear entire cache index in memory and file cache for a specific collection defined through [collectionProperties]
     */
    @JvmStatic
    @SuppressLint("CommitPrefEdits")
    fun clearCollection(collectionProperties: CollectionProperties, context: Context) {
        // clear shared preferences - shared prefs file is still going to exist
        getPrefs(collectionProperties, context).edit().clear().apply()

        // unregister shared prefs instance
        getMetaPrefs(context)
            .edit()
            .remove(getPrefKey(collectionProperties))
            .apply()
    }

    /**
     * clears cache index in both memory and file cache
     */
    @JvmStatic
    @SuppressLint("CommitPrefEdits")
    fun clear(context: Context) {
        // clear all shared preferences
        val metaPrefs = getMetaPrefs(context)
        val allPrefs = metaPrefs.all
        for ((sharedPrefsName) in allPrefs) {
            // clear preferences
            context.getSharedPreferences(sharedPrefsName, 0).edit().clear().apply()
            // delete shared preferences' backup file
            val sharedPrefsFile = FilePaths.getSharedPrefsFile(sharedPrefsName, context)
            if (sharedPrefsFile.exists()) {
                sharedPrefsFile.delete()
            } else {
                IonLog.w("Cache Index Store", "Clear: Shared Prefs File " + sharedPrefsFile.path + " not found.")
            }
        }

        // unregister shared prefs
        metaPrefs.edit().clear().apply()
    }

    private fun getPrefs(collectionProperties: CollectionProperties, context: Context): SharedPreferences =
        context.getSharedPreferences(getPrefKey(collectionProperties), 0)

    private fun getPrefKey(collectionProperties: CollectionProperties): String =
        "cache_index_" + collectionProperties.collectionIdentifier + "_" + collectionProperties.locale + "_" + collectionProperties.variation

    private fun getMetaPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences("cache_index_meta", 0)
}
