package com.anfema.ionclient

class IonConfig @JvmOverloads constructor(
    /**
     * base URL pointing to the ION endpoint
     */
    @JvmField
    val baseUrl: String,

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
    val archiveDownloads: Boolean = false,

    /**
     * Time after which collection is refreshed = fetched from server again.
     */
    @JvmField
    val minutesUntilCollectionRefetch: Int = DEFAULT_MINUTES_UNTIL_COLLECTION_REFETCH,
) {
    companion object {
        const val DEFAULT_VARIATION = "default"
        const val DEFAULT_MINUTES_UNTIL_COLLECTION_REFETCH = 5
    }

    init {
        assert(baseUrl.contains("://"))
        assert(collectionIdentifier.isNotEmpty())
        assert(locale.isNotEmpty())
        assert(variation.isNotEmpty())
    }
}
