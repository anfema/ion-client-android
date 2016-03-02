package com.anfema.ionclient.exceptions;

import java.io.IOException;

public class PageNotAvailableException extends IOException
{
	public PageNotAvailableException()
	{
		super( "Page is not in cache and no internet connection is available." );
	}
}
