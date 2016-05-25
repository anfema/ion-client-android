package com.anfema.ionclient.pages.models

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSyntaxException
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.junit.Test
import kotlin.test.*

class MetaTest
{
    val metaEmpty1 = Meta()
    val metaEmpty2 = Meta()

    val json12 = mapOf("key" to JsonPrimitive("value"))
    val json3 = mapOf("key3" to JsonPrimitive("<p>Harley Davidson Headquarter,<br>Neu-Isenburg, Germany,<br>HERADESIGN® <i>fine</i></p>"))

    val meta1 = Meta()
    val meta2 = Meta()
    val meta3 = Meta()

    init
    {
        meta1.json = json12
        meta2.json = json12
        meta3.json = json3
    }

    @Test
    @Throws(Exception::class)
    fun equals_contract()
    {
        EqualsVerifier.forClass(Meta::class.java).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    fun equals_null()
    {
        assertFalse { metaEmpty1.equals(null) }
    }

    @Test
    fun equals_twoEmpty()
    {
        assertTrue { metaEmpty1.equals(metaEmpty2) }
        assertTrue { metaEmpty2.equals(metaEmpty1) }
    }

    @Test
    fun equals_twoNonEmpty_equal()
    {
        assertTrue { meta1.equals(meta2) }
        assertTrue { meta2.equals(meta1) }
    }

    @Test
    fun equals_twoNonEmpty_different()
    {
        assertFalse { meta1.equals(meta3) }
        assertFalse { meta3.equals(meta1) }
    }

    @Test
    fun contains_empty()
    {
        assertFalse { metaEmpty1.contains("key") }
    }

    @Test
    fun contains_not()
    {
        assertFalse { meta1.contains("bla") }
    }

    @Test
    fun contains_success()
    {
        assertTrue { meta1.contains("key") }
    }

    @Test
    fun containsAllowNull_empty()
    {
        assertFalse { metaEmpty1.contains("key") }
    }

    @Test
    fun containsAllowNull_not()
    {
        val meta = Meta()
        meta.json = mapOf("key" to null)
        assertFalse { meta1.contains("bla") }
    }

    @Test
    fun containsAllowNull_success()
    {
        val meta = Meta()
        meta.json = mapOf("key" to null)
        assertTrue { meta1.contains("key") }
    }

    @Test
    fun getPlainTextOrThrow_throwsNPE()
    {
        assertFailsWith<NullPointerException> { metaEmpty1.getPlainTextOrThrow("key") }
    }

    @Test
    fun getPlainTextOrThrow_throwsJsonSyntaxEx()
    {
        val meta = Meta()
        meta.json = mapOf("key" to JsonObject())
        assertFailsWith<JsonSyntaxException> { meta.getPlainTextOrThrow("key") }
    }

    @Test
    fun getPlainTextOrThrow_success()
    {
        assertEquals(meta1.getPlainTextOrThrow("key"), "value")
    }

    @Test
    fun getPlainTextOrNull_emptyReturnsNull()
    {
        assertNull (metaEmpty1.getTextOrNull("key"))
    }

    @Test
    fun getPlainTextOrNull_invalidTypeReturnsNull()
    {
        val meta = Meta()
        meta.json = mapOf("key" to JsonObject())
        assertNull (meta.getPlainTextOrNull("key"))
    }

    @Test
    fun getPlainTextOrNull_success()
    {
        assertEquals(meta1.getPlainTextOrNull("key"), "value")
    }

    @Test
    fun getPlainTextOrEmpty_emptyReturnsEmptyString()
    {
        assertEquals (metaEmpty1.getPlainTextOrEmpty("key"), "")
    }

    @Test
    fun getPlainTextOrEmpty_invalidTypeReturnsEmptyString()
    {
        val meta = Meta()
        meta.json = mapOf("key" to JsonObject())
        assertEquals (meta.getPlainTextOrEmpty("key"), "")
    }

    @Test
    fun getPlainTextOrEmpty_success()
    {
        assertEquals(meta1.getPlainTextOrEmpty("key"), "value")
    }

    @Test
    fun formattedVsPlain_difference()
    {
        assertNotEquals(meta3.getTextOrThrow("key3"), meta3.getPlainTextOrThrow("key3"))
    }

    @Test
    fun formattedVsPlain_common()
    {
        assertEquals(meta3.getTextOrThrow("key3").toString(), meta3.getPlainTextOrThrow("key3"))
    }
}