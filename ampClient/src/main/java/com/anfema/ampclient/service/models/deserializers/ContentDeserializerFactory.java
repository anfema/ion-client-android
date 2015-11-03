package com.anfema.ampclient.service.models.deserializers;

import com.anfema.ampclient.service.models.contents.ColorContent;
import com.anfema.ampclient.service.models.contents.ContainerContent;
import com.anfema.ampclient.service.models.contents.DatetimeContent;
import com.anfema.ampclient.service.models.contents.FileContent;
import com.anfema.ampclient.service.models.contents.FlagContent;
import com.anfema.ampclient.service.models.contents.ImageContent;
import com.anfema.ampclient.service.models.contents.MediaContent;
import com.anfema.ampclient.service.models.contents.OptionContent;
import com.anfema.ampclient.service.models.contents.TextContent;

public class ContentDeserializerFactory
{
	public static ContentDeserializer newInstance()
	{
		ContentDeserializer deserializer = new ContentDeserializer();
		// register all content subtypes
		deserializer.registerContentType( "colorcontent", ColorContent.class );
		deserializer.registerContentType( "containercontent", ContainerContent.class );
		deserializer.registerContentType( "datetimecontent", DatetimeContent.class );
		deserializer.registerContentType( "filecontent", FileContent.class );
		deserializer.registerContentType( "flagcontent", FlagContent.class );
		deserializer.registerContentType( "imagecontent", ImageContent.class );
		deserializer.registerContentType( "mediacontent", MediaContent.class );
		deserializer.registerContentType( "optioncontent", OptionContent.class );
		deserializer.registerContentType( "textcontent", TextContent.class );
		return deserializer;
	}
}
