package com.anfema.ionclient.exceptions;

import java.io.IOException;

public class HttpException extends IOException
{
	public final int    code;
	public final String message;

	public HttpException( int code, String message )
	{
		this.code = code;
		this.message = message;
	}
}
