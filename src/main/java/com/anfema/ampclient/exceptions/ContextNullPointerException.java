package com.anfema.ampclient.exceptions;

public class ContextNullPointerException extends IllegalArgumentException
{
	public ContextNullPointerException()
	{
		super( "Context must not be null." );
	}
}
