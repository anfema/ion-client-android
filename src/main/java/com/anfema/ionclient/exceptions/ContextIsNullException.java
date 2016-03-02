package com.anfema.ionclient.exceptions;

public class ContextIsNullException extends IllegalArgumentException
{
	public ContextIsNullException()
	{
		super( "Context must not be null." );
	}
}
