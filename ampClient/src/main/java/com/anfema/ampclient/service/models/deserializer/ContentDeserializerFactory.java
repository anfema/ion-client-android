package com.anfema.ampclient.service.models.deserializer;

import com.anfema.ampclient.service.models.ColorContent;
import com.anfema.ampclient.service.models.ContainerContent;

public class ContentDeserializerFactory
{
	public static ContentDeserializer newInstance()
	{
		ContentDeserializer deserializer = new ContentDeserializer();
		// register all content subtypes
		deserializer.registerContentType( "colorcontent", ColorContent.class );
		deserializer.registerContentType( "containercontent", ContainerContent.class );
		return deserializer;
	}
}
