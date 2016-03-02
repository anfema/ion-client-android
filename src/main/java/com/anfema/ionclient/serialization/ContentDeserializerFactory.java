package com.anfema.ionclient.serialization;

import com.anfema.ionclient.pages.models.contents.ColorContent;
import com.anfema.ionclient.pages.models.contents.ConnectionContent;
import com.anfema.ionclient.pages.models.contents.ContainerContent;
import com.anfema.ionclient.pages.models.contents.DatetimeContent;
import com.anfema.ionclient.pages.models.contents.FileContent;
import com.anfema.ionclient.pages.models.contents.FlagContent;
import com.anfema.ionclient.pages.models.contents.ImageContent;
import com.anfema.ionclient.pages.models.contents.MediaContent;
import com.anfema.ionclient.pages.models.contents.NumberContent;
import com.anfema.ionclient.pages.models.contents.OptionContent;
import com.anfema.ionclient.pages.models.contents.TextContent;

/**
 * This is where content types are registered.
 */
public class ContentDeserializerFactory
{
	public static ContentDeserializer newInstance()
	{
		ContentDeserializer deserializer = new ContentDeserializer();
		registerAllContentTypes( deserializer );
		return deserializer;
	}

	/**
	 * List all subtypes of IContent which shall be deserialized.
	 */
	private static void registerAllContentTypes( ContentDeserializer deserializer )
	{
		deserializer.registerContentType( "colorcontent", ColorContent.class );
		deserializer.registerContentType( "connectioncontent", ConnectionContent.class );
		deserializer.registerContentType( "containercontent", ContainerContent.class );
		deserializer.registerContentType( "datetimecontent", DatetimeContent.class );
		deserializer.registerContentType( "filecontent", FileContent.class );
		deserializer.registerContentType( "flagcontent", FlagContent.class );
		deserializer.registerContentType( "imagecontent", ImageContent.class );
		deserializer.registerContentType( "mediacontent", MediaContent.class );
		deserializer.registerContentType( "numbercontent", NumberContent.class );
		deserializer.registerContentType( "optioncontent", OptionContent.class );
		deserializer.registerContentType( "textcontent", TextContent.class );
	}
}
