package com.anfema.ionclient.exceptions;

import java.io.IOException;

import okhttp3.HttpUrl;

public class FileNotAvailableException extends IOException
{
	public FileNotAvailableException( HttpUrl url )
	{
		super( "Media file " + url + " is not in cache and no internet connection is available." );
	}
}
