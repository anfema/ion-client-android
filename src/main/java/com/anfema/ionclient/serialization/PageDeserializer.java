package com.anfema.ionclient.serialization;

import com.anfema.ionclient.pages.models.Page;
import com.anfema.ionclient.pages.models.contents.ConnectionContent;
import com.anfema.ionclient.pages.models.contents.ContainerContent;
import com.anfema.ionclient.pages.models.contents.Content;
import com.anfema.ionclient.utils.ListUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Hook into deserialization of {@link Page} to sort flatten content access and sort contents by position.
 */
public class PageDeserializer implements JsonDeserializer<Page>
{
	private static Gson gson = new GsonBuilder()
			// parse content subtypes
			.registerTypeAdapter( Content.class, ContentDeserializerFactory.newInstance() )
			.registerTypeAdapter( ConnectionContent.class, new ConnectionContentSerializer() )
			// parse datetime strings (trying two patterns)
			.registerTypeAdapter( DateTime.class, new DateTimeSerializer() )
			.create();

	@Override
	public Page deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context ) throws JsonParseException
	{
		Page page = gson.fromJson( json, Page.class );

		if ( page.contents == null )
		{
			page.contents = new ArrayList<>();
		}

		if ( page.contents.size() == 1 )
		{
			ContainerContent containerContent = ( ContainerContent ) page.contents.get( 0 );
			if ( containerContent != null )
			{
				page.contents = containerContent.children;
				ListUtils.sort( page.contents );
			}
		}
		return page;
	}
}
