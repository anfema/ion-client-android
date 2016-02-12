package com.anfema.ampclient.serialization;

import com.anfema.ampclient.pages.models.contents.AContent;
import com.anfema.ampclient.pages.models.contents.EmptyContent;
import com.anfema.ampclient.utils.Log;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ContentDeserializer implements JsonDeserializer<AContent>
{
	// element name where content type is stored in Json
	private final String ELEMENT_NAME_FOR_TYPE = "type";


	Map<String, Class<? extends AContent>> contentTypeRegistry = new HashMap<>();

	void registerContentType( String typeName, Class<? extends AContent> type )
	{
		contentTypeRegistry.put( typeName, type );
	}

	@Override
	public AContent deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException
	{
		JsonObject jsonObject = json.getAsJsonObject();

		// check if content is unavailable
		JsonElement isAvailableElement = jsonObject.get( "is_available" );
		boolean isAvailable = isAvailableElement == null || isAvailableElement.getAsBoolean();

		Class<? extends AContent> type;
		if ( isAvailable )
		{
			// determine content type
			String typeName = jsonObject.get( ELEMENT_NAME_FOR_TYPE ).getAsString();
			type = contentTypeRegistry.get( typeName );

			if ( type == null )
			{
				Log.w( "Content deserialization failed because no type is registered for " + typeName + "." );
				return null;
			}
		}
		else
		{
			type = EmptyContent.class;
		}
		return context.deserialize( jsonObject, type );
	}
}
