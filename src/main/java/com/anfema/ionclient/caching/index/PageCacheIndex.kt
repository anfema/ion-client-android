package com.anfema.ionclient.caching.index

import android.content.Context
import com.anfema.ionclient.IonConfig
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
        fun retrieve(requestUrl: String, config: IonConfig, context: Context): PageCacheIndex? =
            CacheIndexStore.retrieve(requestUrl, config, context)

        @JvmStatic
        fun save(page: Page, config: IonConfig, context: Context) {
            save(page.identifier, page.last_changed, config, context)
        }

        @JvmStatic
        fun save(pageIdentifier: String?, pageLastChanged: DateTime, config: IonConfig, context: Context) {
            val url = IonPageUrls.getPageUrl(config, pageIdentifier)
            val cacheIndex = PageCacheIndex(pageLastChanged)
            CacheIndexStore.save(url, cacheIndex, config, context)
        }
    }
}
