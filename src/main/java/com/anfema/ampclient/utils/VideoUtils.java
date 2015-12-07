package com.anfema.ampclient.utils;

import android.net.Uri;
import android.widget.VideoView;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class VideoUtils
{
	/**
	 * Setting headers for video URI in {@link VideoView} is possible with method {@link VideoView#setVideoURI(Uri, Map)} from API level 21.
	 * This method is a bit hacky. It enables setting headers by use reflection to access a private field.
	 */
	public static void setHeaders( VideoView videoView, Map<String, String> headersMap )
	{
		try
		{
			Field field = VideoView.class.getDeclaredField( "mHeaders" );
			field.setAccessible( true );
			field.set( videoView, headersMap );
		}
		catch ( Exception e )
		{
			Log.ex( e );
		}
	}

	/**
	 * Setting headers for video URI in {@link VideoView} is possible with method {@link VideoView#setVideoURI(Uri, Map)} from API level 21.
	 * This method is a bit hacky. It enables setting headers by use reflection to access a private field.
	 */
	public static void setAuthorizationHeader( VideoView videoView, String authHeaderValue )
	{
		Map<String, String> headers = new HashMap<>();
		headers.put( "Authorization", authHeaderValue );
		setHeaders( videoView, headers );
	}
}
