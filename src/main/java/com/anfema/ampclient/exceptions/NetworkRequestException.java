package com.anfema.ampclient.exceptions;

import java.io.IOException;

public class NetworkRequestException extends IOException
{
	public NetworkRequestException( String url, Throwable cause )
	{
		super( "Failure on network request " + url, cause );
	}
}
