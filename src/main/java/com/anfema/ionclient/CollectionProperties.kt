package com.anfema.ionclient

class CollectionProperties @JvmOverloads constructor(
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
) {
    companion object {
        const val DEFAULT_VARIATION = "default"
    }

    init {
        assert(baseUrl.contains("://"))
        assert(collectionIdentifier.isNotEmpty())
        assert(locale.isNotEmpty())
        assert(variation.isNotEmpty())
    }
}
