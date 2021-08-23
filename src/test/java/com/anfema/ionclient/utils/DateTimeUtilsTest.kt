package com.anfema.ionclient.utils

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateTimeUtilsTest {

    @Test
    fun testRoundtripsForDateTimes() {
        testRoundtripDateTime(DateTimeUtils.now())
        testRoundtripDateTime(DateTimeUtils.now().withZone(DateTimeZone.UTC))
    }

    private fun testRoundtripDateTime(original: DateTime) {
        println("original toString(): $original")
        val intermediate = DateTimeUtils.toString(original)
        println("intermediate String: $intermediate")
        val afterRoundtrip = DateTimeUtils.parseOrThrow(intermediate)
        println("afterRoundtrip toString(): $afterRoundtrip")
        assertTrue(original.isEqual(afterRoundtrip))
        assertEquals(original, afterRoundtrip)
    }

    @Test
    fun testRoundtripsForStrings() {
        testRoundtripString("2015-11-12T20:52:51Z")
        testRoundtripString("2015-11-12T20:52:51+02:00")
    }

    private fun testRoundtripString(original: String) {
        println("original String: $original")
        val intermediate = DateTimeUtils.parseOrThrow(original)
        println("intermediate toString(): $intermediate")
        val afterRoundtrip = DateTimeUtils.toString(intermediate)
        println("afterRoundtrip String: $afterRoundtrip")
        assertEquals(original, afterRoundtrip)
    }
}
