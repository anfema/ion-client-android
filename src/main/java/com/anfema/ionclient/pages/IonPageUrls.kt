package com.anfema.ionclient.pages

import com.anfema.ionclient.CollectionProperties
import com.anfema.ionclient.exceptions.NoIonPagesRequestException
import com.anfema.ionclient.utils.FileUtils
import java.util.regex.Pattern

internal object IonPageUrls {
    const val SLASH = FileUtils.SLASH
    const val QUERY_BEGIN = "?"
    const val QUERY_VARIATION = "variation="
    private val MEDIA_URL_INDICATORS = arrayOf("/media/", "/protected_media/")
    private const val ARCHIVE_URL_INDICATOR = ".tar"

    @JvmStatic
    @Throws(NoIonPagesRequestException::class)
    fun analyze(url: String, collectionProperties: CollectionProperties): IonRequestInfo {
        return if (isMediaRequestUrl(url)) {
            IonRequestInfo(IonRequestType.MEDIA, null, null, null, null)
        } else if (isArchiveUrl(url)) {
            IonRequestInfo(IonRequestType.ARCHIVE, null, null, null, null)
        } else {
            val relativeUrlPath = url.replace(collectionProperties.baseUrl, "")
            val urlPathSegments = relativeUrlPath.split(SLASH).toTypedArray()
            if (urlPathSegments.size < 2 || urlPathSegments.size > 3) {
                throw NoIonPagesRequestException(url)
            }
            val idPlusVariation =
                urlPathSegments[urlPathSegments.size - 1].split(Pattern.quote("?")).toTypedArray()
            val locale: String
            val variation: String
            if (idPlusVariation.size == 2) {
                locale = urlPathSegments[0]
                variation = idPlusVariation[1]
            } else if (idPlusVariation.size == 1) {
                locale = urlPathSegments[0]
                variation = CollectionProperties.DEFAULT_VARIATION
            } else {
                throw NoIonPagesRequestException(url)
            }
            val collectionIdentifier: String
            if (urlPathSegments.size == 2) {
                collectionIdentifier = idPlusVariation[0]
                IonRequestInfo(IonRequestType.COLLECTION, locale, variation, collectionIdentifier, null)
            } else  // urlPathSegments.length == 3
            {
                collectionIdentifier = urlPathSegments[1]
                val pageIdentifier = idPlusVariation[0]
                IonRequestInfo(IonRequestType.PAGE, locale, variation, collectionIdentifier, pageIdentifier)
            }
        }
    }

    private fun isMediaRequestUrl(url: String): Boolean {
        for (mediaIndicator in MEDIA_URL_INDICATORS) {
            if (url.contains(mediaIndicator)) {
                return true
            }
        }
        return false
    }

    private fun isArchiveUrl(url: String): Boolean {
        return url.endsWith(ARCHIVE_URL_INDICATOR) || url.contains("$ARCHIVE_URL_INDICATOR?")
    }

    @JvmStatic
    fun getCollectionUrl(collectionProperties: CollectionProperties): String {
        return collectionProperties.baseUrl + collectionProperties.locale + SLASH + collectionProperties.collectionIdentifier + QUERY_BEGIN + QUERY_VARIATION + collectionProperties.variation
    }

    @JvmStatic
    fun getPageUrl(collectionProperties: CollectionProperties, pageId: String): String {
        return collectionProperties.baseUrl + collectionProperties.locale + SLASH + collectionProperties.collectionIdentifier + SLASH + pageId + QUERY_BEGIN + QUERY_VARIATION + collectionProperties.variation
    }

    enum class IonRequestType {
        COLLECTION, PAGE, MEDIA, ARCHIVE
    }

    internal class IonRequestInfo(
        var requestType: IonRequestType,
        var locale: String?,
        var variation: String?,
        var collectionIdentifier: String?,
        var pageIdentifier: String?,
    )
}
