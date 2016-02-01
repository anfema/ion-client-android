package com.anfema.ampclient;

import android.net.Uri;
import android.widget.VideoView;

import com.anfema.ampclient.utils.Log;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * This utility methods are only relevant if minimum supported API level is below 21. Otherwise, headers can be set with {@link VideoView#setVideoURI(Uri, Map)}.
 */
public class VideoUtils
{
	/**
	 * Setting headers for video URI in {@link VideoView} is possible with method {@link VideoView#setVideoURI(Uri, Map)} from API level 21.
	 * This method is a bit hacky. It enables setting headers by use reflection to access a private field.
	 * <p>
	 * Call this method INSTEAD OF {@link VideoView#setVideoURI(Uri)}. DO NOT CALL {@link VideoView#setVideoURI(Uri)} afterwards, in which case "mHeaders" field would be set to null again!
	 */
	public static void setVideoUri( VideoView videoView, Uri videoUri, Map<String, String> headersMap )
	{
		videoView.setVideoURI( videoUri );
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
	 * Set authorization header for video URI in {@link VideoView}.
	 * <p>
	 * Call this method AFTER {@link VideoView#setVideoURI(Uri)} because otherwise "mHeaders" field is set to null again!
	 *
	 * @see #setVideoUri(VideoView, Uri, Map)
	 */
	public static void setVideoUri( VideoView videoView, Uri videoUri, String authHeaderValue )
	{
		Map<String, String> headers = new HashMap<>();
		headers.put( "Authorization", authHeaderValue );
		setVideoUri( videoView, videoUri, headers );
	}
}
