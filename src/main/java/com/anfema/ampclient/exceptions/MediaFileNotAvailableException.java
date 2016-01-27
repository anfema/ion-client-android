package com.anfema.ampclient.exceptions;

import java.io.IOException;

import okhttp3.HttpUrl;

public class MediaFileNotAvailableException extends IOException
{
	public MediaFileNotAvailableException( HttpUrl url )
	{
		super( "Media file " + url + " is not in cache and no internet connection is available." );
	}
}
