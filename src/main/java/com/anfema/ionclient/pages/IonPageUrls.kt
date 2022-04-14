package com.anfema.ionclient.pages

import com.anfema.ionclient.CollectionProperties
import com.anfema.ionclient.exceptions.NoIonPagesRequestException
import com.anfema.ionclient.utils.FileUtils.SLASH
import okhttp3.HttpUrl.Companion.toHttpUrl

internal object IonPageUrls {

    private const val QUERY_VARIATION = "variation"
    private val MEDIA_URL_INDICATORS = arrayOf("/media/", "/protected_media/")
    private const val ARCHIVE_URL_INDICATOR = ".tar"

    sealed interface IonRequestType {
        object Collection : IonRequestType
        class Page(val pageIdentifier: String) : IonRequestType
        object Media : IonRequestType
        object Archive : IonRequestType
    }

    @JvmStatic
    @Throws(NoIonPagesRequestException::class)
    fun getRequestType(url: String, baseUrl: String): IonRequestType =
        when {
            isMediaRequestUrl(url) -> IonRequestType.Media
            isArchiveUrl(url) -> IonRequestType.Archive
            else -> {
                val httpUrl = url.toHttpUrl()

                val ionSegments = let {
                    val baseUrlSegments = baseUrl.toHttpUrl().pathSegments.filter { it.isNotEmpty() }
                    val urlSegments = httpUrl.pathSegments
                    urlSegments.subList(baseUrlSegments.size, urlSegments.size)
                }

                if (ionSegments.size !in 2..3) {
                    throw NoIonPagesRequestException(url)
                }

                val pageIdentifier = ionSegments.getOrNull(2)

                if (pageIdentifier == null) {
                    IonRequestType.Collection
                } else {
                    IonRequestType.Page(pageIdentifier)
                }
            }
        }

    private fun isMediaRequestUrl(url: String) =
        MEDIA_URL_INDICATORS.any { mediaIndicator -> url.contains(mediaIndicator) }

    private fun isArchiveUrl(url: String) =
        url.endsWith(ARCHIVE_URL_INDICATOR) || url.contains("$ARCHIVE_URL_INDICATOR?")

    @JvmStatic
    fun CollectionProperties.getCollectionUrl(): String =
        (baseUrl + locale + SLASH + collectionIdentifier)
            .addVariation(variation)

    @JvmStatic
    fun CollectionProperties.getPageUrl(pageId: String): String =
        (baseUrl + locale + SLASH + collectionIdentifier + SLASH + pageId)
            .addVariation(variation)

    private fun String.addVariation(variation: String): String {
        return toHttpUrl()
            .newBuilder().addQueryParameter(QUERY_VARIATION, variation).build()
            .toString()
    }
}
