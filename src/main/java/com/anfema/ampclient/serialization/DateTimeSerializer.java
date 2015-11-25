package com.anfema.ampclient.serialization;

import com.anfema.ampclient.utils.DateTimeUtils;
import com.anfema.ampclient.utils.Log;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.text.ParseException;

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
			return DateTimeUtils.parseDateTime( dateString );
		}
		catch ( ParseException e )
		{
			Log.e( "Amp Deserializer", json.toString() );
			Log.ex( e );
			return null;
		}
	}
}
