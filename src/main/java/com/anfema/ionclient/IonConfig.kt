package com.anfema.ionclient

import android.content.Context
import com.anfema.ionclient.exceptions.IonConfigInvalidException
import com.anfema.utils.EqualsContract
import java.util.Arrays

class IonConfig {
    /**
     * base URL pointing to the ION endpoint
     */
    @JvmField
    val baseUrl: String?

    /**
     * the collection identifier, [IonClient] will use for its calls
     */
    @JvmField
    val collectionIdentifier: String?

    /**
     * Which language shall be requested? (e.g. "de_DE")
     */
    @JvmField
    val locale: String?

    /**
     * For which platform/resolution are pages requested?
     */
    @JvmField
    val variation: String

    /**
     * Should the whole archive be downloaded when the collection is downloaded?
     */
    @JvmField
    val archiveDownloads: Boolean

    /**
     * Time after which collection is refreshed = fetched from server again.
     */
    val minutesUntilCollectionRefetch: Int

    class Builder(private val baseUrl: String, private val collectionIdentifier: String) {
        private var locale: String? = null
        private var variation = DEFAULT_VARIATION
        private var archiveDownloads = false
        private var minutesUntilCollectionRefetch = DEFAULT_MINUTES_UNTIL_COLLECTION_REFETCH
        fun locale(locale: String?): Builder {
            this.locale = locale
            return this
        }

        /**
         * Set locale from device configuration
         */
        fun locale(context: Context): Builder {
            locale = context.resources.configuration.locale.toString()
            return this
        }

        fun variation(variation: String): Builder {
            this.variation = variation
            return this
        }

        fun archiveDownloads(archiveDownloads: Boolean): Builder {
            this.archiveDownloads = archiveDownloads
            return this
        }

        fun minutesUntilCollectionRefetch(minutesUntilCollectionRefetch: Int): Builder {
            this.minutesUntilCollectionRefetch = minutesUntilCollectionRefetch
            return this
        }

        fun build(): IonConfig {
            val config = IonConfig(
                baseUrl,
                collectionIdentifier,
                locale,
                variation,
                archiveDownloads,
                minutesUntilCollectionRefetch
            )
            assertConfigIsValid(config)
            return config
        }
    }

    constructor(
        baseUrl: String?,
        collectionIdentifier: String?,
        locale: String?,
        variation: String,
        archiveDownloads: Boolean,
        minutesUntilCollectionRefetch: Int,
    ) {
        this.baseUrl = baseUrl
        this.collectionIdentifier = collectionIdentifier
        this.locale = locale
        this.variation = variation
        this.archiveDownloads = archiveDownloads
        this.minutesUntilCollectionRefetch = minutesUntilCollectionRefetch
    }

    constructor(otherConfig: IonConfig) {
        baseUrl = otherConfig.baseUrl
        collectionIdentifier = otherConfig.collectionIdentifier
        locale = otherConfig.locale
        variation = otherConfig.variation
        archiveDownloads = otherConfig.archiveDownloads
        minutesUntilCollectionRefetch = otherConfig.minutesUntilCollectionRefetch
    }

    val isValid: Boolean
        get() = (baseUrl != null && baseUrl.contains("://")
            && collectionIdentifier != null && locale != null && locale.length > 0)

    /**
     * To check that attributes are equal which are ESSENTIAL for the IDENTITY of ION client
     */
    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj !is IonConfig) {
            return false
        }
        val other = obj
        return (EqualsContract.equal(baseUrl, other.baseUrl)
            && EqualsContract.equal(collectionIdentifier, other.collectionIdentifier)
            && EqualsContract.equal(locale, other.locale)
            && EqualsContract.equal(variation, other.variation))
    }

    override fun hashCode(): Int {
        val hashRelevantFields = arrayOf<Any?>(baseUrl, collectionIdentifier, locale, variation)
        return Arrays.hashCode(hashRelevantFields)
    }

    companion object {
        const val DEFAULT_VARIATION = "default"
        const val DEFAULT_MINUTES_UNTIL_COLLECTION_REFETCH = 5
        fun assertConfigIsValid(config: IonConfig?) {
            if (config == null || !config.isValid) {
                throw IonConfigInvalidException()
            }
        }
    }
}
