package com.anfema.ampclient.exceptions;

public class AuthorizationHeaderValueIsNullException extends IllegalArgumentException
{
	public AuthorizationHeaderValueIsNullException()
	{
		super( "Authorization header value must not be null." );
	}
}
