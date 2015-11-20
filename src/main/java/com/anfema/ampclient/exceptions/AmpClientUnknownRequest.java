package com.anfema.ampclient.exceptions;

import java.io.IOException;

public class AmpClientUnknownRequest extends IOException
{
	public AmpClientUnknownRequest( String url )
	{
		super( "Unknown request: " + url );
	}

	public AmpClientUnknownRequest()
	{
		super();
	}
}
