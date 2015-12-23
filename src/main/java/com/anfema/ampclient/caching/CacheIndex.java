package com.anfema.ampclient.caching;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.anfema.ampclient.serialization.GsonHolder;
import com.squareup.okhttp.HttpUrl;

public abstract class CacheIndex
{
	private String filename;

	public CacheIndex( String filename )
	{
		this.filename = filename;
	}

	/**
	 * Use MD5 of request URL as filename
	 */
	public CacheIndex( HttpUrl requestUrl )
	{
		this.filename = FilePaths.getFileName( requestUrl.toString() );
	}

	public String getFilename()
	{
		return filename;
	}

	public void setFilename( String filename )
	{
		this.filename = filename;
	}

	// Persistence - shared preferences

	private static final String PREFS_CACHING_INDEX = "prefs_caching_index";

	public static <T extends CacheIndex> T retrieve( String requestUrl, Context context, Class<T> cacheMetaSubclass )
	{
		SharedPreferences prefs = getPrefs( context );
		String json = prefs.getString( requestUrl, null );
		return GsonHolder.getInstance().fromJson( json, cacheMetaSubclass );
	}

	public static <T extends CacheIndex> void save( String requestUrl, T cacheMeta, Context context )
	{
		SharedPreferences prefs = getPrefs( context );
		Editor editor = prefs.edit();
		editor.putString( requestUrl, GsonHolder.getInstance().toJson( cacheMeta ) );
		editor.apply();
	}

	private static SharedPreferences getPrefs( Context context )
	{
		return context.getSharedPreferences( PREFS_CACHING_INDEX, 0 );
	}
}
