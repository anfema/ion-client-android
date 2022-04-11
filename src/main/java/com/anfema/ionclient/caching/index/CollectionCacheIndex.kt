package com.anfema.ionclient.caching.index

import android.content.Context
import com.anfema.ionclient.CollectionProperties
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

    fun isOutdated(collectionRefetchIntervalInMin: Int): Boolean =
        lastUpdated.isBefore(DateTimeUtils.now().minusMinutes(collectionRefetchIntervalInMin))

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
        fun retrieve(collectionProperties: CollectionProperties, context: Context): CollectionCacheIndex? {
            val requestUrl = IonPageUrls.getCollectionUrl(collectionProperties)
            return CacheIndexStore.retrieve(requestUrl, collectionProperties, context)
        }

        @JvmStatic
        fun save(
            collectionProperties: CollectionProperties,
            context: Context,
            lastModified: String?,
            lastUpdated: DateTime?,
        ) {
            val url = IonPageUrls.getCollectionUrl(collectionProperties)
            val cacheIndex = CollectionCacheIndex(
                lastUpdated ?: DateTimeUtils.now(),
                lastModified,
            )
            CacheIndexStore.save(url, cacheIndex, collectionProperties, context)
        }
    }
}
