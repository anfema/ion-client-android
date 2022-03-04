package com.anfema.ionclient.caching.index

import android.content.Context
import com.anfema.ionclient.IonConfig
import com.anfema.ionclient.utils.DateTimeUtils
import com.anfema.ionclient.utils.IonLog
import com.anfema.utils.HashUtils
import org.joda.time.DateTime
import java.io.File

data class FileCacheIndex(
    /**
     * checksum of file version in local storage
     */
    val checksum: String,
    /**
     * Needs to be set to the time, the file is saved
     */
    val lastUpdated: DateTime,
) : CacheIndex() {

    fun isOutdated(serverChecksum: String): Boolean =
        checksum != serverChecksum

    companion object {

        @JvmStatic
        fun retrieve(requestUrl: String, config: IonConfig, context: Context): FileCacheIndex? {
            return CacheIndexStore.retrieve(requestUrl, FileCacheIndex::class.java, config, context)
        }

        /**
         * @param checksum can be null
         */
        @JvmStatic
        fun save(
            requestUrl: String,
            file: File?,
            config: IonConfig,
            checksum: String?,
            requestTime: DateTime?,
            context: Context,
        ) {
            if (file == null) {
                IonLog.e("File Cache Index", "Could not save cache index for $requestUrl\nBecause file is null.")
                return
            }

            val cacheIndex = FileCacheIndex(
                checksum ?: "sha256:" + HashUtils.getSha256(file),
                requestTime ?: DateTimeUtils.now(),
            )
            CacheIndexStore.save(requestUrl, cacheIndex, config, context)
        }
    }
}
