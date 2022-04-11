package com.anfema.ionclient

class IonConfig @JvmOverloads constructor(
    /**
     * base URL pointing to the ION endpoint
     */
    @JvmField
    val pagesBaseUrl: String,

    /**
     * the collection identifier, [IonClient] will use for its calls
     */
    @JvmField
    val collectionIdentifier: String,

    /**
     * Which language shall be requested? (e.g. "de_DE")
     */
    @JvmField
    val locale: String,

    /**
     * For which platform/resolution are pages requested?
     */
    @JvmField
    val variation: String = DEFAULT_VARIATION,

    /**
     * Should the whole archive be downloaded when the collection is downloaded?
     */
    @JvmField
    val automaticArchiveDownloads: Boolean = false,

    /**
     * Time after which collection is refreshed = fetched from server again.
     */
    @JvmField
    val collectionRefetchIntervalInMin: Int = DEFAULT_COLLECTION_REFETCH_INTERVAL_IN_MIN,
) {
    companion object {
        const val DEFAULT_VARIATION = "default"
        const val DEFAULT_COLLECTION_REFETCH_INTERVAL_IN_MIN = 5
    }

    init {
        assert(pagesBaseUrl.contains("://"))
        assert(collectionIdentifier.isNotEmpty())
        assert(locale.isNotEmpty())
        assert(variation.isNotEmpty())
    }
}
