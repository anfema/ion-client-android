package com.anfema.ionclient.pages.models

import com.anfema.ionclient.exceptions.PageNotInCollectionException
import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import org.joda.time.DateTime

internal class Collection(
    /**
     * ION internal id of the collection
     */
    private val id: String?,
    /**
     * Usually, there is one collection that is used per app. Thus, the collection identifier can be hard-coded and passed to ION client configuration.
     * The identifier matches the collection_identifier in the API requests.
     */
    val identifier: String?,

    /**
     * displayable name of the collection
     */
    val name: String?,
    /**
     * e.g "de_DE"
     */
    @SerializedName("default_locale")
    private val defaultLocale: String?,
    /**
     * Zip file containing all pages and files of collection.
     */
    val archive: String?,
    /**
     * page previews and meta information about them
     */
    val pages: List<PagePreview> = emptyList(),
) : SizeAware {

    fun getPageLastChangedAsync(pageIdentifier: String): Single<DateTime> =
        try {
            Single.just(getPageLastChanged(pageIdentifier))
        } catch (e: PageNotInCollectionException) {
            Single.error(e)
        }

    @Throws(PageNotInCollectionException::class)
    fun getPageLastChanged(pageIdentifier: String): DateTime =
        (pages.find { pagePreview -> pagePreview.identifier == pageIdentifier }
            ?: throw PageNotInCollectionException(identifier, pageIdentifier))
            .last_changed

    var byteCount: Long = 0

    override fun byteCont(): Long = byteCount

    override fun toString(): String =
        """Collection [
            |id = $id, 
            |identifier = $identifier, 
            |name = $name, 
            |default_locale = $defaultLocale, 
            |archive $archive, 
            |pages = $pages
            |]""".trimMargin()
}
