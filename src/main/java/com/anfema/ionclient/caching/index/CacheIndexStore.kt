package com.anfema.ionclient.caching.index

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.anfema.ionclient.IonConfig
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
        config: IonConfig,
        context: Context,
    ): T? {
        // check shared preferences
        IonLog.d("Index Lookup", "$requestUrl from shared preferences")
        val prefs = getPrefs(config, context)
        val json = prefs.getString(requestUrl, null)
        val index = GsonHolder.getInstance().fromJson(json, T::class.java)
        if (index != null) {
            // make cache index aware of its size by storing byte count to its field
            index.byteCount = json?.byteCount()?.toInt() ?: 0
        }
        return index
    }

    fun <T : CacheIndex> save(requestUrl: String, cacheIndex: T, config: IonConfig, context: Context) {
        IonLog.d("Cache Index", "saving index for $requestUrl")
        try {
            val file = FilePaths.getFilePath(requestUrl, config, context)
            if (file.exists() && file.length() > 0) {
                // make cache index aware of its size by storing byte count to its field
                val indexSerialized = GsonHolder.getInstance().toJson(cacheIndex)
                cacheIndex.byteCount = indexSerialized.byteCount().toInt()

                // save to shared preferences
                getPrefs(config, context)
                    .edit()
                    .putString(requestUrl, indexSerialized)
                    .apply()

                // register shared prefs instance
                getMetaPrefs(context)
                    .edit()
                    .putBoolean(getPrefKey(config), true)
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
    fun delete(requestUrl: String, config: IonConfig, context: Context) {
        IonLog.d("Cache Index", "deleting index for $requestUrl")

        // delete from shared preferences
        getPrefs(config, context)
            .edit()
            .remove(requestUrl)
            .apply()
    }

    /**
     * @param config to determine collection
     * @return all index entry URLs of collection
     */
    @JvmStatic
    fun retrieveAllUrls(config: IonConfig, context: Context): Set<String> {
        // check shared preferences
        IonLog.d("Cache Index", "Retrieve all index entries of collection " + config.collectionIdentifier)
        val prefs = getPrefs(config, context)
        val urls: MutableSet<String> = HashSet()
        for ((key) in prefs.all) {
            urls.add(key)
        }
        return urls
    }

    /**
     * clear entire cache index in memory and file cache for a specific collection defined through {@param config}
     */
    @JvmStatic
    @SuppressLint("CommitPrefEdits")
    fun clearCollection(config: IonConfig, context: Context) {
        // clear shared preferences - shared prefs file is still going to exist
        getPrefs(config, context).edit().clear().apply()

        // unregister shared prefs instance
        getMetaPrefs(context)
            .edit()
            .remove(getPrefKey(config))
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

    private fun getPrefs(config: IonConfig, context: Context): SharedPreferences =
        context.getSharedPreferences(getPrefKey(config), 0)

    private fun getPrefKey(config: IonConfig): String =
        "cache_index_" + config.collectionIdentifier + "_" + config.locale + "_" + config.variation

    private fun getMetaPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences("cache_index_meta", 0)
}
