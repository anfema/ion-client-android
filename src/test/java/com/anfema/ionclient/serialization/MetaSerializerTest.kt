package com.anfema.ionclient.serialization;

import com.anfema.ionclient.pages.models.Meta
import com.anfema.ionclient.pages.models.PagePreview
import com.google.gson.JsonPrimitive
import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MetaSerializerTest : Spek({
    given("gson serializer, serialized and object meta data", {
        val gson = GsonHolder.getInstance()

        val serializedMeta = "{\"description\":\"Harley Davidson Headquarter,Neu-Isenburg, Germany,HERADESIGNfine\"}"

        val metaJson = mapOf("title" to JsonPrimitive("Hallo"))
        val meta = Meta()
        meta.json = metaJson

        context("Deserialization", {
            on("deserialize into Meta object", {
                val metaDeserialized = gson.fromJson(serializedMeta, Meta::class.java)

                it("should create Meta object and store json meta data in raw field", {
                    assertNotNull(metaDeserialized?.json)
                })
                it("should contain test property description", {
                    val descriptionText = metaDeserialized.getPlainTextOrNull("description")
                    assertNotNull(descriptionText)
                })
            })
            given("empty meta data", {
                on("deserialize page preview", {
                    val metaEmpty = gson.fromJson(JsonPrimitive(""), Meta::class.java)

                    it("should create Meta object", {
                        assertNotNull(metaEmpty)
                    })
                    it("should not have any data in meta", {
                        // assertEquals(gson.toJson(pagePreview.meta), ppMetaPart)
                        assertNull(metaEmpty.json)
                    })
                })
            })
        })
        context("Serialization", {
            val pagePreview = PagePreview()

            it("should have non-null Meta object", {
                assertNotNull(pagePreview.meta)
            })

            on("serialize page preview without meta data", {
                val ppSerialized = gson.toJson(pagePreview)
                it("should not contain property name 'meta'", {
                    assert(!ppSerialized.contains("meta"))
                })
            })
            on("serialize page preview with meta data", {
                pagePreview.meta.json = metaJson
                val ppSerialized = gson.toJson(pagePreview)
                it("should contain property name 'meta'", {
                    assert(ppSerialized.contains("meta"))
                })
            })
        })
        context("Roundtrip", {
            on ("serialize + deserialize", {
                val metaSerialized = gson.toJson(meta)
                val metaDeserialized = gson.fromJson(metaSerialized, Meta::class.java)
                it("(roundtrip meta object) should be equal to original object", {
                    assertEquals(meta, metaDeserialized)
                })
            })
            on ("deserialize + serialize", {
                val metaDeserialized = gson.fromJson(serializedMeta, Meta::class.java)
                val metaSerialized = gson.toJson(metaDeserialized)
                it("(roundtrip meta string) should be equal to original serialized string", {
                    assertEquals(serializedMeta, metaSerialized)
                })
            })
        })
    })
})
