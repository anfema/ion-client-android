package com.anfema.ampclient.exceptions;

import java.io.IOException;

public class CollectionNotAvailableException extends IOException
{
	public CollectionNotAvailableException()
	{
		super( "Collection is not in cache and no internet connection is available." );
	}
}
