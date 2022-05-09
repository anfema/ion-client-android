package com.anfema.ionclient.pages

import com.anfema.ionclient.CollectionProperties
import com.anfema.ionclient.pages.IonPageUrls.IonRequestType
import com.anfema.ionclient.pages.IonPageUrls.getCollectionUrl
import com.anfema.ionclient.pages.IonPageUrls.getPageUrl
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertIs

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
    fun getRequestType_collection() {
        val request = IonPageUrls.getRequestType(collectionUrl, props.baseUrl)

        assertIs<IonRequestType.Collection>(request)
    }

    @Test
    fun getRequestType_collection_with_more_segments() {
        val request = IonPageUrls.getRequestType(collectionUrlMoreSegments, baseUrlMoreSegments)

        assertIs<IonRequestType.Collection>(request)
    }

    @Test
    fun getRequestType_page() {
        val request = IonPageUrls.getRequestType(pageUrl, props.baseUrl)

        assertIs<IonRequestType.Page>(request)
        assertEquals(pageIdentifier, request.pageIdentifier)
    }

    @Test
    fun getRequestType_archive() {
        val request = IonPageUrls.getRequestType(archiveUrl, props.baseUrl)

        assertIs<IonRequestType.Archive>(request)
    }

    @Test
    fun getRequestType_media() {
        val request = IonPageUrls.getRequestType(mediaUrl, props.baseUrl)

        assertIs<IonRequestType.Media>(request)
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
