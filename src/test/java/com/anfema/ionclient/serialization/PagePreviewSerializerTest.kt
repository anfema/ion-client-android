package com.anfema.ionclient.serialization

import com.anfema.ionclient.pages.models.PagePreview
import com.google.gson.JsonPrimitive
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PagePreviewSerializerTest : Spek({
    val gson = GsonHolder.defaultInstance

    context("Deserialization", {
        given("a serialized page preview without meta data", {
            val pagePreviewWithoutMeta = "{\"identifier\": \"harley-davidson-zentrale\",\"collection_identifier\": \"lookbook\",\"version_number\": 1,\"last_changed\": \"2016-04-28T06:46:00Z\",\"position\": 1,\"layout\": \"project\",\"locale\": \"de_DE\",\"parent\": \"commercial\",\"first_published_at\": \"2016-04-28T06:46:00.854\"}"
            on("deserialize page preview", {
                val pagePreview = gson.fromJson(pagePreviewWithoutMeta, PagePreview::class.java)

                it("should create Meta object", {
                    assertNotNull(pagePreview?.meta)
                })
                it("should not have any data in meta", {
                    // assertEquals(gson.toJson(pagePreview.meta), ppMetaPart)
                    assertNull(pagePreview.meta.json)
                })
            })
        })
    })
    context("Serialization", {
        given("an empty page preview", {
            val pagePreview = PagePreview()

            it("it should have non-null Meta object", {
                assertNotNull(pagePreview.meta)
            })

            on("serialize page preview without meta data", {
                val ppSerialized = gson.toJson(pagePreview)
                it("should not contain property name 'meta'", {
                    assert(!ppSerialized.contains("meta"))
                })
            })
            on("serialize page preview with meta data", {
                pagePreview.meta.json = mapOf("title" to JsonPrimitive("Hallo"))
                val ppSerialized = gson.toJson(pagePreview)
                it("should contain property name 'meta'", {
                    assert(ppSerialized.contains("meta"))
                })
            })
        })
    })
})
