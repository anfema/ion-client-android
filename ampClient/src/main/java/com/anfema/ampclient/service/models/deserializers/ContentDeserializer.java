package com.anfema.ampclient.service.models.deserializers;

import com.anfema.ampclient.service.models.contents.AContent;
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


	Map<String, Class<? extends AContent>> contentTypeRegistry = new HashMap<String, Class<? extends AContent>>();

	void registerContentType( String typeName, Class<? extends AContent> type )
	{
		contentTypeRegistry.put( typeName, type );
	}

	@Override
	public AContent deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException
	{
		JsonObject jsonObject = json.getAsJsonObject();

		// determine content type
		String typeName = jsonObject.get( ELEMENT_NAME_FOR_TYPE ).getAsString();
		Class<? extends AContent> type = contentTypeRegistry.get( typeName );
		if ( type == null )
		{
			return null;
			//			throw new JsonParseException( "Content deserialization failed because type " + typeName + " is unknown." );
		}

		return context.deserialize( jsonObject, type );
	}
}
