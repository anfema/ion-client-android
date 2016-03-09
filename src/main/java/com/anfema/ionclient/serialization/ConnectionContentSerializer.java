package com.anfema.ionclient.serialization;

import android.net.Uri;
import android.net.Uri.Builder;

import com.anfema.ionclient.pages.models.contents.ConnectionContent;
import com.anfema.ionclient.pages.models.contents.Content;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Class doing both serialization and deserialization of ConnectionContent objects.
 */
public class ConnectionContentSerializer implements JsonDeserializer<ConnectionContent>, JsonSerializer<ConnectionContent>
{
	public static final String CONNECTION_STRING = "connection_string";

	@Override
	public JsonElement serialize( ConnectionContent connection, Type typeOfSrc, JsonSerializationContext context )
	{
		JsonObject jsonObject = new Gson().toJsonTree( connection, Content.class ).getAsJsonObject();

		Builder uriBuilder = new Builder()
				.scheme( connection.scheme )
				.authority( connection.collectionIdentifier );
		for ( String pageIdentifier : connection.pageIdentifierPath )
		{
			uriBuilder.appendPath( pageIdentifier );
		}
		Uri connectionUri = uriBuilder
				.fragment( connection.contentIdentifier )
				.build();
		String connectionString = connectionUri.toString();
		jsonObject.add( CONNECTION_STRING, new JsonPrimitive( connectionString ) );
		return jsonObject;
	}

	@Override
	public ConnectionContent deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException
	{
		JsonObject jsonObject = json.getAsJsonObject();
		Content content = new Gson().fromJson( jsonObject, Content.class );
		String connectionString = jsonObject.get( CONNECTION_STRING ).getAsString();

		return new ConnectionContent( content, connectionString );
	}
}
