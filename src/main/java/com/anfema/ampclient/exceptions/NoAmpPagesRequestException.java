package com.anfema.ampclient.exceptions;

public class NoAmpPagesRequestException extends Exception
{
	public NoAmpPagesRequestException( String message )
	{
		super( message );
	}

	public NoAmpPagesRequestException()
	{
		super();
	}
}
