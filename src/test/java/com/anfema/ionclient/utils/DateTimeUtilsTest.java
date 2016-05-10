package com.anfema.ionclient.utils;

import junit.framework.TestCase;

import org.joda.time.DateTime;

/**
 * Created by peterfischer on 25.11.15.
 */
public class DateTimeUtilsTest extends TestCase
{
	public void testRoundtripDateTime() throws Exception
	{
		DateTime original = DateTimeUtils.now();
		System.out.println( "original toString(): " + original );
		String intermediate = DateTimeUtils.toString( original );
		System.out.println( "intermediate String: " + intermediate );
		DateTime afterRoundtrip = DateTimeUtils.parseOrThrow( intermediate );
		System.out.println( "afterRoundtrip toString(): " + afterRoundtrip );
		assertTrue( original.isEqual( afterRoundtrip ) );
		assertEquals( original, afterRoundtrip );
	}

	public void testRoundtripString() throws Exception
	{
		String original = "2015-11-12T20:52:51Z";
		System.out.println( "original String: " + original );
		DateTime intermediate = DateTimeUtils.parseOrThrow( original );
		System.out.println( "intermediate toString(): " + intermediate );
		String afterRoundtrip = DateTimeUtils.toString( intermediate );
		System.out.println( "afterRoundtrip String: " + afterRoundtrip );
		assertEquals( original, afterRoundtrip );
	}
}