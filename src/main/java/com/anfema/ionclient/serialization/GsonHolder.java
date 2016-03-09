package com.anfema.ionclient.serialization;

import android.support.annotation.NonNull;

import com.anfema.ionclient.pages.models.Collection;
import com.anfema.ionclient.pages.models.Page;
import com.anfema.ionclient.pages.models.contents.ConnectionContent;
import com.anfema.ionclient.pages.models.contents.Content;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;

public class GsonHolder
{
	private static Gson gsonInstance = createGson();

	public static Gson getInstance()
	{
		return gsonInstance;
	}

	@NonNull
	private static Gson createGson()
	{
		return new GsonBuilder()
				// parse content subtypes
				.registerTypeAdapter( Content.class, ContentDeserializerFactory.newInstance() )
				.registerTypeAdapter( ConnectionContent.class, new ConnectionContentSerializer() )
				// parse datetime strings (trying two patterns)
				.registerTypeAdapter( DateTime.class, new DateTimeSerializer() )
				.registerTypeAdapter( Collection.class, new CollectionDeserializer() )
				.registerTypeAdapter( Page.class, new PageDeserializer() )
				.create();
	}
}
