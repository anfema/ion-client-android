package com.anfema.ampclient.exceptions;

import java.io.IOException;

public class ReadFromCacheException extends IOException
{
	public ReadFromCacheException( String url )
	{
		super( "Unable to read from local storage. URL: " + url );
	}
}
