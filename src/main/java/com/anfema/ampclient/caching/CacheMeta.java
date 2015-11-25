package com.anfema.ampclient.caching;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.anfema.ampclient.serialization.GsonFactory;
import com.anfema.ampclient.utils.FileUtils;
import com.google.gson.Gson;
import com.squareup.okhttp.HttpUrl;

public abstract class CacheMeta
{
	private String filename;

	public CacheMeta( String filename )
	{
		this.filename = filename;
	}

	/**
	 * Use MD5 of request URL as filename
	 */
	public CacheMeta( HttpUrl requestUrl )
	{
		this.filename = FileUtils.calcMD5( requestUrl.toString() );
	}

	public String getFilename()
	{
		return filename;
	}

	public void setFilename( String filename )
	{
		this.filename = filename;
	}

	// ****** Store and retrieve from shared preferences ***********

	private static final String PREFS_CACHING_META = "prefs_caching_meta";
	private static final Gson   gson               = GsonFactory.newInstance();

	public static <T extends CacheMeta> T retrieve( String requestUrl, Context context, Class<T> cacheMetaSubclass )
	{
		SharedPreferences prefs = getPrefs( context );
		String json = prefs.getString( requestUrl, null );
		return gson.fromJson( json, cacheMetaSubclass );
	}

	public static <T extends CacheMeta> void save( String requestUrl, T cacheMeta, Context context )
	{
		SharedPreferences prefs = getPrefs( context );
		Editor editor = prefs.edit();
		editor.putString( requestUrl, gson.toJson( cacheMeta ) );
		editor.apply();
	}

	private static SharedPreferences getPrefs( Context context )
	{
		return context.getSharedPreferences( PREFS_CACHING_META, 0 );
	}
}
