package com.anfema.ionclient.serialization;

import com.anfema.ionclient.pages.models.Collection;
import com.anfema.ionclient.utils.ListUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.joda.time.DateTime;

import java.lang.reflect.Type;

/**
 * Hook into deserialization of {@link Collection} to sort page previews by position.
 */
public class CollectionDeserializer implements JsonDeserializer<Collection>
{
	private static Gson gson = new GsonBuilder()
			// parse datetime strings (trying two patterns)
			.registerTypeAdapter( DateTime.class, new DateTimeSerializer() )
			.create();

	@Override
	public Collection deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException
	{
		Collection collection = gson.fromJson( json, Collection.class );
		ListUtils.sort( collection.pages );
		return collection;
	}
}
