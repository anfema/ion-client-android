package com.anfema.ionclient.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateTimeUtilsTest {

    @Test
    fun testRoundtripDateTime() {
        val original = DateTimeUtils.now()
        println("original toString(): $original")
        val intermediate = DateTimeUtils.toString(original)
        println("intermediate String: $intermediate")
        val afterRoundtrip = DateTimeUtils.parseOrThrow(intermediate)
        println("afterRoundtrip toString(): $afterRoundtrip")
        assertTrue(original.isEqual(afterRoundtrip))
        assertEquals(original, afterRoundtrip)
    }

    @Test
    fun testRoundtripString() {
        val original = "2015-11-12T20:52:51Z"
        println("original String: $original")
        val intermediate = DateTimeUtils.parseOrThrow(original)
        println("intermediate toString(): $intermediate")
        val afterRoundtrip = DateTimeUtils.toString(intermediate)
        println("afterRoundtrip String: $afterRoundtrip")
        assertEquals(original, afterRoundtrip)
    }
}
