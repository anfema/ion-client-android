package com.anfema.ampclient.exceptions;

import java.io.IOException;

public class UnknownAmpRequest extends IOException
{
	public UnknownAmpRequest( String url )
	{
		super( "Unknown request: " + url );
	}

	public UnknownAmpRequest()
	{
		super();
	}
}
