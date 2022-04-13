package com.anfema.ionclient.pages

import com.anfema.ionclient.CollectionProperties
import com.anfema.ionclient.CollectionProperties.Companion.DEFAULT_VARIATION
import com.anfema.ionclient.pages.IonPageUrls.getCollectionUrl
import com.anfema.ionclient.pages.IonPageUrls.getPageUrl
import org.junit.Assert.assertEquals
import org.junit.Test

class IonPageUrlsTest {

    private val collectionIdentifier = "bcgyou"
    private val locale = "en_US"
    private val collectionUrl = "https://www.career-app.bcg.com/v1/en_US/bcgyou?variation=default"
    private val baseUrlMoreSegments = "https://www.career-app.bcg.com/foo/v1/"
    private val collectionUrlMoreSegments = "https://www.career-app.bcg.com/foo/v1/en_US/bcgyou?variation=default"

    private val pageIdentifier = "bigdaynavigationrootc742a5e4-8bcb-4690-ad71-e7725ddb81c8"
    private val pageUrl =
        "https://www.career-app.bcg.com/v1/en_US/bcgyou/bigdaynavigationrootc742a5e4-8bcb-4690-ad71-e7725ddb81c8?variation=default"

    private val archiveUrl = "https://www.career-app.bcg.com/v1/en_US/bcgyou-namr.tar?variation=default"
    private val mediaUrl = "https://www.career-app.bcg.com/media/images/BCG_AD_office.jpegquality-70.jpg"

    private val props = CollectionProperties(
        baseUrl = "https://www.career-app.bcg.com/v1/",
        collectionIdentifier = collectionIdentifier,
        locale = locale,
    )

    @Test
    fun analyze_collection() {
        val requestInfo = IonPageUrls.analyze(collectionUrl, props.baseUrl)

        assertEquals(IonPageUrls.IonRequestType.COLLECTION, requestInfo.requestType)
        assertEquals(collectionIdentifier, requestInfo.collectionIdentifier)
        assertEquals(locale, requestInfo.locale)
        assertEquals(null, requestInfo.pageIdentifier)
        assertEquals(DEFAULT_VARIATION, requestInfo.variation)
    }

    @Test
    fun analyze_collection_with_more_segments() {
        val requestInfo = IonPageUrls.analyze(collectionUrlMoreSegments, baseUrlMoreSegments)

        assertEquals(IonPageUrls.IonRequestType.COLLECTION, requestInfo.requestType)
        assertEquals(collectionIdentifier, requestInfo.collectionIdentifier)
        assertEquals(locale, requestInfo.locale)
        assertEquals(null, requestInfo.pageIdentifier)
        assertEquals(DEFAULT_VARIATION, requestInfo.variation)
    }

    @Test
    fun analyze_page() {
        val requestInfo = IonPageUrls.analyze(pageUrl, props.baseUrl)

        assertEquals(IonPageUrls.IonRequestType.PAGE, requestInfo.requestType)
        assertEquals(collectionIdentifier, requestInfo.collectionIdentifier)
        assertEquals(locale, requestInfo.locale)
        assertEquals(pageIdentifier, requestInfo.pageIdentifier)
        assertEquals(DEFAULT_VARIATION, requestInfo.variation)
    }

    @Test
    fun analyze_archive() {
        val requestInfo = IonPageUrls.analyze(archiveUrl, props.baseUrl)

        assertEquals(IonPageUrls.IonRequestType.ARCHIVE, requestInfo.requestType)
        assertEquals(null, requestInfo.collectionIdentifier)
        assertEquals(null, requestInfo.locale)
        assertEquals(null, requestInfo.pageIdentifier)
        assertEquals(null, requestInfo.variation)
    }

    @Test
    fun analyze_media() {
        val requestInfo = IonPageUrls.analyze(mediaUrl, props.baseUrl)

        assertEquals(IonPageUrls.IonRequestType.MEDIA, requestInfo.requestType)
        assertEquals(null, requestInfo.collectionIdentifier)
        assertEquals(null, requestInfo.locale)
        assertEquals(null, requestInfo.pageIdentifier)
        assertEquals(null, requestInfo.variation)
    }

    @Test
    fun getCollectionUrl() {
        assertEquals(collectionUrl, props.getCollectionUrl())
    }

    @Test
    fun getPageUrl() {
        assertEquals(pageUrl, props.getPageUrl(pageIdentifier))
    }
}
