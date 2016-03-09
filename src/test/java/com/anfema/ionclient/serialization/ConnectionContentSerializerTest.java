package com.anfema.ionclient.serialization;

import com.anfema.ionclient.pages.models.contents.ConnectionContent;
import com.anfema.ionclient.pages.models.contents.Content;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class ConnectionContentSerializerTest extends TestCase
{
	Gson gson;

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		gson = GsonHolder.getInstance();
	}

	public void testRoundtripConnectionContent() throws Exception
	{
		ConnectionContent original = new ConnectionContent( null );
		original.scheme = "ion";
		original.collectionIdentifier = "collection_identifier";
		List<String> pageIdentifierPath = new ArrayList<>();
		pageIdentifierPath.add( "page_identifier1" );
		String pageIdentifier = "page_identifier2";
		pageIdentifierPath.add( pageIdentifier );
		original.pageIdentifierPath = pageIdentifierPath;
		original.pageIdentifier = pageIdentifier;
		original.contentIdentifier = "content_identifier";
		System.out.println( "original toString(): " + original );


		String intermediate = gson.toJson( original );
		System.out.println( "intermediate String: " + intermediate );
		ConnectionContent afterRoundtrip = gson.fromJson( intermediate, ConnectionContent.class );
		System.out.println( "afterRoundtrip toString(): " + afterRoundtrip );
		assertEquals( original, afterRoundtrip );
	}

	public void testRoundtripRelativeUrl() throws Exception
	{
		String original = "//collection_identifier/page_identifier";
		roundtripUrl( original );
	}

	public void testRoundtripAbsoluteUrl() throws Exception
	{
		String original = "ion://collection_identifier/page_identifier1/page_identifier2#content_identfier";
		roundtripUrl( original );
	}

	private void roundtripUrl( String original )
	{
		System.out.println( "original String: " + original );

		JsonObject jsonObject = gson.toJsonTree( new Content() ).getAsJsonObject();
		jsonObject.add( ConnectionContentSerializer.CONNECTION_STRING, new JsonPrimitive( original ) );

		ConnectionContent intermediate = gson.fromJson( jsonObject, ConnectionContent.class );
		System.out.println( "intermediate toString(): " + intermediate );
		String afterRoundtrip = gson.toJsonTree( intermediate ).getAsJsonObject().get( ConnectionContentSerializer.CONNECTION_STRING ).getAsString();
		System.out.println( "afterRoundtrip String: " + afterRoundtrip );
		assertEquals( original, afterRoundtrip );
	}

	/**
	 * Tests that deserializing an empty string returns null
	 */
	public void testDeserializeEmptyString()
	{
		ConnectionContent intermediate = gson.fromJson( "", ConnectionContent.class );
		assert ( intermediate == null );
	}

	/**
	 * Tests that deserializing a null string returns null
	 */
	public void testDeserializeNullString()
	{
		ConnectionContent intermediate = gson.fromJson( ( String ) null, ConnectionContent.class );
		assert ( intermediate == null );
	}
}