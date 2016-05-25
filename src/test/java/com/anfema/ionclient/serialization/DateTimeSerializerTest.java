package com.anfema.ionclient.serialization;

import com.anfema.ionclient.utils.DateTimeUtils;
import com.google.gson.Gson;

import junit.framework.TestCase;

import org.joda.time.DateTime;

/**
 * Created by peterfischer on 25.11.15.
 */
public class DateTimeSerializerTest extends TestCase
{
	Gson gson;

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		gson = GsonHolder.getInstance();
	}

	public void testRoundtripDateTime() throws Exception
	{
		DateTime original = DateTimeUtils.now();
		System.out.println( "original toString(): " + original );
		String intermediate = gson.toJson( original );
		System.out.println( "intermediate String: " + intermediate );
		DateTime afterRoundtrip = gson.fromJson( intermediate, DateTime.class );
		System.out.println( "afterRoundtrip toString(): " + afterRoundtrip );
		assertEquals( original, afterRoundtrip );
	}

	public void testRoundtripString() throws Exception
	{
		String original = "\"2015-11-25T14:45:11Z\"";
		System.out.println( "original String: " + original );
		DateTime intermediate = gson.fromJson( original, DateTime.class );
		System.out.println( "intermediate toString(): " + intermediate );
		String afterRoundtrip = gson.toJson( intermediate );
		System.out.println( "afterRoundtrip String: " + afterRoundtrip );
		assertEquals( original, afterRoundtrip );
	}

	/**
	 * Tests that deserializing an empty string returns null
	 */
	public void testDeserializeEmptyString()
	{
		DateTime intermediate = gson.fromJson( "", DateTime.class );
		assert ( intermediate == null );
	}

	/**
	 * Tests that deserializing a null string returns null
	 */
	public void testDeserializeNullString()
	{
		DateTime intermediate = gson.fromJson( ( String ) null, DateTime.class );
		assert ( intermediate == null );
	}
}