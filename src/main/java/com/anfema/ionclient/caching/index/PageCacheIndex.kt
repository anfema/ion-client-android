package com.anfema.ionclient.caching.index

import android.content.Context
import com.anfema.ionclient.CollectionProperties
import com.anfema.ionclient.pages.IonPageUrls
import com.anfema.ionclient.pages.models.Page
import org.joda.time.DateTime

class PageCacheIndex(
    /**
     * Last changed info received from server in the page response
     */
    private val lastChanged: DateTime,
) : CacheIndex() {

    fun isOutdated(serverDate: DateTime?): Boolean =
        lastChanged.isBefore(serverDate)

    companion object {

        @JvmStatic
        fun retrieve(
            requestUrl: String,
            collectionProperties: CollectionProperties,
            context: Context,
        ): PageCacheIndex? =
            CacheIndexStore.retrieve(requestUrl, collectionProperties, context)

        @JvmStatic
        fun save(page: Page, collectionProperties: CollectionProperties, context: Context) {
            save(page.identifier, page.last_changed, collectionProperties, context)
        }

        @JvmStatic
        fun save(
            pageIdentifier: String,
            pageLastChanged: DateTime,
            collectionProperties: CollectionProperties,
            context: Context,
        ) {
            val url = IonPageUrls.getPageUrl(collectionProperties, pageIdentifier)
            val cacheIndex = PageCacheIndex(pageLastChanged)
            CacheIndexStore.save(url, cacheIndex, collectionProperties, context)
        }
    }
}
