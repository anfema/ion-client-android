package com.anfema.ionclient.exceptions;

public class NoIonPagesRequestException extends Exception
{
	public NoIonPagesRequestException( String message )
	{
		super( message );
	}

	public NoIonPagesRequestException()
	{
		super();
	}
}
