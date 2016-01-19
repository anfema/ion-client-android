package com.anfema.ampclient.caching;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.anfema.ampclient.serialization.GsonHolder;
import com.anfema.ampclient.utils.Log;

public class CacheIndexStore
{
	public static <T extends CacheIndex> T retrieve( String requestUrl, Class<T> cacheIndexSubclass, String collectionIdentifier, Context context )
	{
		// check memory cache
		T index = MemoryCacheIndex.get( requestUrl, collectionIdentifier, cacheIndexSubclass );
		if ( index != null )
		{
			Log.d( "Index Lookup", requestUrl + " from memory" );
			return index;
		}

		// check shared preferences
		Log.d( "Index Lookup", requestUrl + " from shared preferences" );
		SharedPreferences prefs = getPrefs( collectionIdentifier, context );
		String json = prefs.getString( requestUrl, null );
		index = GsonHolder.getInstance().fromJson( json, cacheIndexSubclass );
		// save to memory cache
		if ( index != null )
		{
			MemoryCacheIndex.put( requestUrl, collectionIdentifier, index );
		}
		return index;
	}

	public static <T extends CacheIndex> void save( String requestUrl, T cacheIndex, String collectionIdentifier, Context context )
	{
		// save to memory cache
		MemoryCacheIndex.put( requestUrl, collectionIdentifier, cacheIndex );

		// save to shared preferences
		SharedPreferences prefs = getPrefs( collectionIdentifier, context );
		Editor editor = prefs.edit();
		editor.putString( requestUrl, GsonHolder.getInstance().toJson( cacheIndex ) );
		editor.apply();
	}

	public static void clear( String collectionIdentifier, Context context )
	{
		// clear memory cache
		MemoryCacheIndex.clear( collectionIdentifier );

		// clear shared preferences
		SharedPreferences prefs = getPrefs( collectionIdentifier, context );
		prefs.edit().clear().commit();
	}

	public static SharedPreferences getPrefs( String collectionIdentifier, Context context )
	{
		return context.getSharedPreferences( "prefs_cache_index_" + collectionIdentifier, 0 );
	}
}