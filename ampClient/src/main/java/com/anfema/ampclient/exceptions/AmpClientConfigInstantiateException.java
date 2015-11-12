package com.anfema.ampclient.exceptions;

public class AmpClientConfigInstantiateException extends InstantiationException
{
	public AmpClientConfigInstantiateException()
	{
		super( "Your AmpClientConfig implementation must provide public default constructor without parameters." );
	}
}
