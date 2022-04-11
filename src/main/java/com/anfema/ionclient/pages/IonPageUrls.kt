package com.anfema.ionclient.pages

import com.anfema.ionclient.CollectionProperties
import com.anfema.ionclient.exceptions.NoIonPagesRequestException
import com.anfema.ionclient.utils.FileUtils.SLASH
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.util.regex.Pattern

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
    fun analyze(url: String, collectionProperties: CollectionProperties): IonRequestInfo {
        return when {
            isMediaRequestUrl(url) -> {
                IonRequestInfo(IonRequestType.MEDIA, null, null, null, null)
            }
            isArchiveUrl(url) -> {
                IonRequestInfo(IonRequestType.ARCHIVE, null, null, null, null)
            }
            else -> {
                // TODO use HttpUrl in this code block to simplify the destructuring of the URL

                val relativeUrlPath = url.replace(collectionProperties.baseUrl, "")
                val urlPathSegments = relativeUrlPath.split(SLASH).toTypedArray()

                if (urlPathSegments.size !in 2..3) {
                    throw NoIonPagesRequestException(url)
                }

                val idPlusVariation =
                    urlPathSegments[urlPathSegments.lastIndex].split(Pattern.quote("?"))

                val locale: String
                val variation: String
                when (idPlusVariation.size) {
                    2 -> {
                        locale = urlPathSegments[0]
                        variation = idPlusVariation[1]
                    }
                    1 -> {
                        locale = urlPathSegments[0]
                        variation = CollectionProperties.DEFAULT_VARIATION
                    }
                    else -> {
                        throw NoIonPagesRequestException(url)
                    }
                }
                if (urlPathSegments.size == 2) {
                    val collectionIdentifier = idPlusVariation[0]
                    IonRequestInfo(IonRequestType.COLLECTION, locale, variation, collectionIdentifier, null)
                } else  // urlPathSegments.length == 3
                {
                    val collectionIdentifier = urlPathSegments[1]
                    val pageIdentifier = idPlusVariation[0]
                    IonRequestInfo(IonRequestType.PAGE, locale, variation, collectionIdentifier, pageIdentifier)
                }
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
