package com.anfema.ampclient.exceptions;

import java.io.IOException;

public class ReadFromCacheException extends IOException
{
	public ReadFromCacheException( String url, Throwable cause )
	{
		super( "Failure on cache request " + url, cause );
	}
}
