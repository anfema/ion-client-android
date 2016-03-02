package com.anfema.ionclient.exceptions;

public class AuthorizationHeaderValueIsNullException extends IllegalArgumentException
{
	public AuthorizationHeaderValueIsNullException()
	{
		super( "Authorization header value must not be null." );
	}
}
