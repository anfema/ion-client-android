package com.anfema.ionclient.exceptions;

public class IonConfigInvalidException extends IllegalArgumentException
{
	public IonConfigInvalidException()
	{
		super( "The configuration – either base URL, default collection identifier, or authorization header value - is not valid." );
	}
}
