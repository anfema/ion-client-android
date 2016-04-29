package com.anfema.ionclient.serialization;

import com.anfema.ionclient.pages.models.Collection;
import com.anfema.ionclient.pages.models.Meta;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Hook into deserialization of {@link Collection} to sort page previews by position.
 */
public class MetaDeserializer implements JsonDeserializer<Meta>
{
	private static Gson gson = new Gson();

	@Override
	public Meta deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException
	{
		Meta meta = new Meta();
		try
		{
			Type type = new TypeToken<Map<String, JsonElement>>()
			{
			}.getType();
			meta.json = gson.fromJson( json, type );
		}
		catch ( JsonParseException e )
		{
			// field "json" will be null
		}
		return meta;
	}
}
