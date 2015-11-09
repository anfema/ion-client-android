package com.anfema.ampclient.service.deserializers;

import com.anfema.ampclient.utils.DateTimeUtils;
import com.anfema.ampclient.utils.Log;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.text.ParseException;

public class DateTimeDeserializer implements JsonDeserializer<DateTime>
{
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
			Log.ex( e );
			return null;
		}
	}
}
