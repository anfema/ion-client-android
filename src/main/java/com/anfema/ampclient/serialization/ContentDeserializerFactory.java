package com.anfema.ampclient.serialization;

import com.anfema.ampclient.pages.models.contents.ColorContent;
import com.anfema.ampclient.pages.models.contents.ConnectionContent;
import com.anfema.ampclient.pages.models.contents.ContainerContent;
import com.anfema.ampclient.pages.models.contents.DatetimeContent;
import com.anfema.ampclient.pages.models.contents.FileContent;
import com.anfema.ampclient.pages.models.contents.FlagContent;
import com.anfema.ampclient.pages.models.contents.ImageContent;
import com.anfema.ampclient.pages.models.contents.MediaContent;
import com.anfema.ampclient.pages.models.contents.NumberContent;
import com.anfema.ampclient.pages.models.contents.OptionContent;
import com.anfema.ampclient.pages.models.contents.TextContent;

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
	 * List all subtypes of AContent which shall be deserialized.
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
