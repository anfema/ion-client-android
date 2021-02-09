package com.anfema.ionclient.serialization;

import com.anfema.ionclient.pages.models.contents.Content;
import com.anfema.ionclient.pages.models.contents.EmptyContent;
import com.anfema.ionclient.utils.IonLog;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ContentDeserializer implements JsonDeserializer<Content>
{
	// element name where content type is stored in Json
	private static final String ELEMENT_NAME_FOR_TYPE   = "type";
	private static final String ELEMENT_NAME_FOR_OUTLET = "outlet";

	Map<String, Class<? extends Content>> contentTypeRegistry = new HashMap<>();

	void registerContentType( String typeName, Class<? extends Content> type )
	{
		contentTypeRegistry.put( typeName, type );
	}

	@Override
	public Content deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException
	{
		JsonObject jsonObject = json.getAsJsonObject();

		// check if content is unavailable
		JsonElement isAvailableElement = jsonObject.get( "is_available" );
		boolean isAvailable = isAvailableElement == null || isAvailableElement.getAsBoolean();

		Class<? extends Content> type;
		if ( isAvailable )
		{
			// determine content type
			JsonElement typeElement = jsonObject.get( ELEMENT_NAME_FOR_TYPE );
			if ( typeElement == null )
			{
				JsonElement outletElement = jsonObject.get( ELEMENT_NAME_FOR_OUTLET );
				String outletName = outletElement != null ? outletElement.getAsString() : "n/a";
				IonLog.w( "Content deserialization failed because no type was sent. Outlet: " + outletName );
				return null;
			}
			String typeName = typeElement.getAsString();
			type = contentTypeRegistry.get( typeName );

			if ( type == null )
			{
				IonLog.w( "Content deserialization failed because no type is registered for " + typeName + "." );
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
