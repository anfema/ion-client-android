package com.anfema.ampclient.serialization;

import com.anfema.ampclient.pages.models.Collection;
import com.anfema.ampclient.pages.models.contents.AContent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;

public class GsonHolder
{
	private static Gson gsonInstance;

	public static Gson getInstance()
	{
		if ( gsonInstance == null )
		{
			final GsonBuilder gsonBuilder = new GsonBuilder();
			// parse content subtypes
			gsonBuilder.registerTypeAdapter( AContent.class, ContentDeserializerFactory.newInstance() );
			// parse datetime strings (trying two patterns)
			gsonBuilder.registerTypeAdapter( DateTime.class, new DateTimeSerializer() );
			gsonBuilder.registerTypeAdapter( Collection.class, new CollectionDeserializer() );
			gsonInstance = gsonBuilder.create();
		}
		return gsonInstance;
	}
}
