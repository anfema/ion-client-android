package com.anfema.ionclient.serialization;

import com.anfema.ionclient.pages.models.Collection;
import com.anfema.ionclient.pages.models.Meta;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Hook into deserialization of {@link Collection} to sort page previews by position.
 */
public class MetaSerializer implements JsonDeserializer<Meta>, JsonSerializer<Meta>
{
	@Override
	public JsonElement serialize( Meta meta, Type typeOfSrc, JsonSerializationContext context )
	{
		return context.serialize( meta.json );
	}

	@Override
	public Meta deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException
	{
		Meta meta = new Meta();
		try
		{
			Type type = new TypeToken<Map<String, JsonElement>>()
			{
			}.getType();
			meta.json = context.deserialize( json, type );
		}
		catch ( JsonParseException e )
		{
			// field "json" will be null
		}
		return meta;
	}
}
