package com.anfema.ionclient.pages

import com.anfema.ionclient.CollectionProperties
import com.anfema.ionclient.exceptions.NoIonPagesRequestException
import com.anfema.ionclient.utils.FileUtils.SLASH
import okhttp3.HttpUrl.Companion.toHttpUrl

internal object IonPageUrls {

    private const val QUERY_VARIATION = "variation"
    private val MEDIA_URL_INDICATORS = arrayOf("/media/", "/protected_media/")
    private const val ARCHIVE_URL_INDICATOR = ".tar"

    class IonRequestInfo(
        val requestType: IonRequestType,
        val locale: String?,
        val variation: String?,
        val collectionIdentifier: String?,
        val pageIdentifier: String?,
    )

    enum class IonRequestType {
        COLLECTION, PAGE, MEDIA, ARCHIVE
    }

    @JvmStatic
    @Throws(NoIonPagesRequestException::class)
    fun analyze(url: String, baseUrl: String): IonRequestInfo =
        when {
            isMediaRequestUrl(url) -> {
                IonRequestInfo(IonRequestType.MEDIA, null, null, null, null)
            }
            isArchiveUrl(url) -> {
                IonRequestInfo(IonRequestType.ARCHIVE, null, null, null, null)
            }
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

                IonRequestInfo(
                    requestType = if (pageIdentifier == null) IonRequestType.COLLECTION else IonRequestType.PAGE,
                    locale = ionSegments[0],
                    variation = httpUrl.queryParameter("variation"),
                    collectionIdentifier = ionSegments[1],
                    pageIdentifier = pageIdentifier
                )
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
