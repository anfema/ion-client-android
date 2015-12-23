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

	public static <T extends CacheIndex> T retrieve( String requestUrl, Class<T> cacheMetaSubclass, String collectionIdentifier, Context context )
	{
		SharedPreferences prefs = getPrefs( collectionIdentifier, context );
		String json = prefs.getString( requestUrl, null );
		return GsonHolder.getInstance().fromJson( json, cacheMetaSubclass );
	}

	public static <T extends CacheIndex> void save( String requestUrl, T cacheMeta, String collectionIdentifier, Context context )
	{
		SharedPreferences prefs = getPrefs( collectionIdentifier, context );
		Editor editor = prefs.edit();
		editor.putString( requestUrl, GsonHolder.getInstance().toJson( cacheMeta ) );
		editor.apply();
	}

	public static void clear( String collectionIdentifier, Context context )
	{
		SharedPreferences prefs = getPrefs( collectionIdentifier, context );
		prefs.edit().clear().commit();
	}

	private static SharedPreferences getPrefs( String collectionIdentifier, Context context )
	{
		return context.getSharedPreferences( "prefs_cache_index_" + collectionIdentifier, 0 );
	}
}
