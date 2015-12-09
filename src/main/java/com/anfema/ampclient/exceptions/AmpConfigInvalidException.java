package com.anfema.ampclient.exceptions;

public class AmpConfigInvalidException extends IllegalArgumentException
{
	public AmpConfigInvalidException()
	{
		super( "The configuration – either base URL, default collection identifier, or authorization header value - is not valid." );
	}
}
