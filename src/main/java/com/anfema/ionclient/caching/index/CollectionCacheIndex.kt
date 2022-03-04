package com.anfema.ionclient.caching.index

import android.content.Context
import com.anfema.ionclient.IonConfig
import com.anfema.ionclient.caching.index.CollectionCacheIndex
import com.anfema.ionclient.pages.IonPageUrls
import com.anfema.ionclient.utils.DateTimeUtils
import com.anfema.ionclient.utils.IonLog
import org.joda.time.DateTime

class CollectionCacheIndex(
    /**
     * Last time when collection server call has been made (even if the server response code is 304 NOT MODIFIED)
     */
    private val lastUpdated: DateTime,
    /**
     * Received with the collection server call as response header "Last-Modified"
     */
    val lastModified: String?,
) : CacheIndex() {

    fun isOutdated(config: IonConfig): Boolean =
        lastUpdated.isBefore(DateTimeUtils.now().minusMinutes(config.minutesUntilCollectionRefetch))

    val lastModifiedDate: DateTime?
        get() {
            if (lastModified == null) {
                IonLog.d("Last Modified", "String is null")
                return null
            }
            return try {
                val lastModifiedDate = DateTimeUtils.parseOrThrow(
                    lastModified)
                IonLog.d("Last Modified", "Successfully parsed $lastModified")
                lastModifiedDate
            } catch (e: IllegalArgumentException) {
                IonLog.e("Last Modified", "Parse error for: $lastModified")
                IonLog.ex("Last Modified", e)
                null
            }
        }

    companion object {

        @JvmStatic
        fun retrieve(config: IonConfig, context: Context): CollectionCacheIndex? {
            val requestUrl = IonPageUrls.getCollectionUrl(config)
            return CacheIndexStore.retrieve(requestUrl, CollectionCacheIndex::class.java, config, context)
        }

        @JvmStatic
        fun save(config: IonConfig, context: Context, lastModified: String?, lastUpdated: DateTime?) {
            val url = IonPageUrls.getCollectionUrl(config)
            val cacheIndex = CollectionCacheIndex(
                lastUpdated ?: DateTimeUtils.now(),
                lastModified,
            )
            CacheIndexStore.save(url, cacheIndex, config, context)
        }
    }
}
