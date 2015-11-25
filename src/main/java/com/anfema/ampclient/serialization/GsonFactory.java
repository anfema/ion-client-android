package com.anfema.ampclient.serialization;

import com.anfema.ampclient.models.contents.AContent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;

public class GsonFactory
{
	public static Gson newInstance()
	{
		final GsonBuilder gsonBuilder = new GsonBuilder();
		// parse content subtypes
		gsonBuilder.registerTypeAdapter( AContent.class, ContentDeserializerFactory.newInstance() );
		// parse datetime strings (trying two patterns)
		gsonBuilder.registerTypeAdapter( DateTime.class, new DateTimeSerializer() );
		return gsonBuilder.create();
	}
}
