package com.anfema.ionclient.caching;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.exceptions.NoIonPagesRequestException;
import com.anfema.ionclient.serialization.GsonHolder;
import com.anfema.ionclient.utils.Log;

import java.io.File;

public class CacheIndexStore
{
	public static <T extends CacheIndex> T retrieve( String requestUrl, Class<T> cacheIndexSubclass, IonConfig config, Context context )
	{
		// check memory cache
		T index = MemoryCacheIndex.get( requestUrl, cacheIndexSubclass );
		if ( index != null )
		{
			Log.d( "Index Lookup", requestUrl + " from memory" );
			return index;
		}

		// check shared preferences
		Log.d( "Index Lookup", requestUrl + " from shared preferences" );
		SharedPreferences prefs = getPrefs( config, context );
		String json = prefs.getString( requestUrl, null );
		index = GsonHolder.getInstance().fromJson( json, cacheIndexSubclass );
		// save to memory cache
		if ( index != null )
		{
			MemoryCacheIndex.put( requestUrl, index );
		}
		return index;
	}

	public static <T extends CacheIndex> void save( String requestUrl, T cacheIndex, IonConfig config, Context context )
	{
		Log.d( "Cache Index", "saving index for " + requestUrl );

		try
		{
			File file = FilePaths.getFilePath( requestUrl, config, context );

			if ( file.exists() && file.length() > 0 /* TODO assert file size is just as big as response body length */ )
			{
				// save to memory cache
				MemoryCacheIndex.put( requestUrl, cacheIndex );

				// save to shared preferences
				SharedPreferences prefs = getPrefs( config, context );
				Editor editor = prefs.edit();
				editor.putString( requestUrl, GsonHolder.getInstance().toJson( cacheIndex ) );
				editor.apply();
			}
			else
			{
				Log.e( "Cache Index", "Could not save cache index entry for " + requestUrl + "\nBecause file does not exist." );
			}
		}
		catch ( NoIonPagesRequestException e )
		{
			Log.e( "Cache Index", "Could not save cache index entry for " + requestUrl );
			Log.ex( e );
		}
	}

	@SuppressLint("CommitPrefEdits")
	public static void clear( IonConfig config, Context context )
	{
		// clear entire memory cache
		MemoryCacheIndex.clear();

		// clear shared preferences
		SharedPreferences prefs = getPrefs( config, context );
		prefs.edit().clear().commit();
	}

	public static SharedPreferences getPrefs( IonConfig config, Context context )
	{
		return context.getSharedPreferences( "cache_index_" + config.collectionIdentifier + "_" + config.locale + "_" + config.variation, 0 );
	}
}