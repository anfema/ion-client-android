package com.anfema.ionclient.serialization;

import com.anfema.ionclient.utils.DateTimeUtils;
import com.anfema.ionclient.utils.Log;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;

import java.lang.reflect.Type;

/**
 * Class doing both serialization and deserialization of DateTime objects.
 */
public class DateTimeSerializer implements JsonDeserializer<DateTime>, JsonSerializer<DateTime>
{

	@Override
	public JsonElement serialize( DateTime src, Type typeOfSrc, JsonSerializationContext context )
	{
		return new JsonPrimitive( DateTimeUtils.toString( src ) );
	}

	@Override
	public DateTime deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException
	{
		String dateString = json.getAsString();
		try
		{
			return DateTimeUtils.parseOrThrow( dateString );
		}
		catch ( IllegalArgumentException | NullPointerException e )
		{
			Log.e( "ION Deserializer", json.toString() );
			Log.ex( e );
			return null;
		}
	}
}
