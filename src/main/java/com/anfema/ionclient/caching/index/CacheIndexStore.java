package com.anfema.ionclient.caching.index;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.caching.FilePaths;
import com.anfema.ionclient.exceptions.NoIonPagesRequestException;
import com.anfema.ionclient.serialization.GsonHolder;
import com.anfema.ionclient.utils.IonLog;

import java.io.File;
import java.util.Map;

public class CacheIndexStore
{
	public static <T extends CacheIndex> T retrieve( String requestUrl, Class<T> cacheIndexSubclass, IonConfig config, Context context )
	{
		// check memory cache
		T index = MemoryCacheIndex.get( requestUrl, cacheIndexSubclass );
		if ( index != null )
		{
			IonLog.d( "Index Lookup", requestUrl + " from memory" );
			return index;
		}

		// check shared preferences
		IonLog.d( "Index Lookup", requestUrl + " from shared preferences" );
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
		IonLog.d( "Cache Index", "saving index for " + requestUrl );

		try
		{
			File file = FilePaths.getFilePath( requestUrl, config, context );

			if ( file.exists() && file.length() > 0 )
			{
				// save to memory cache
				MemoryCacheIndex.put( requestUrl, cacheIndex );

				// save to shared preferences
				getPrefs( config, context )
						.edit()
						.putString( requestUrl, GsonHolder.getInstance().toJson( cacheIndex ) )
						.apply();

				// register shared prefs instance
				getMetaPrefs( context )
						.edit()
						.putBoolean( getPrefKey( config ), true )
						.apply();
			}
			else
			{
				IonLog.e( "Cache Index", "Could not save cache index entry for " + requestUrl + "\nBecause file does not exist." );
			}
		}
		catch ( NoIonPagesRequestException e )
		{
			IonLog.e( "Cache Index", "Could not save cache index entry for " + requestUrl );
			IonLog.ex( e );
		}
	}

	/**
	 * clear entire cache index in memory and file cache for a specific collection defined through {@param config}
	 */
	@SuppressLint("CommitPrefEdits")
	public static void clearCollection( IonConfig config, Context context )
	{
		// clear entire memory cache
		MemoryCacheIndex.clear();

		// clear shared preferences - shared prefs file is still going to exist
		getPrefs( config, context ).edit().clear().commit();

		// unregister shared prefs instance
		getMetaPrefs( context )
				.edit()
				.remove( getPrefKey( config ) )
				.apply();
	}

	/**
	 * clears cache index in both memory and file cache
	 */
	@SuppressLint("CommitPrefEdits")
	public static void clear( Context context )
	{
		// clear entire memory cache
		MemoryCacheIndex.clear();

		// clear all shared preferences
		SharedPreferences metaPrefs = getMetaPrefs( context );
		Map<String, ?> allPrefs = metaPrefs.getAll();
		for ( Map.Entry<String, ?> entry : allPrefs.entrySet() )
		{
			String sharedPrefsName = entry.getKey();
			// clear preferences
			context.getSharedPreferences( sharedPrefsName, 0 ).edit().clear().commit();
			// delete shared preferences' backup file
			File sharedPrefsFile = FilePaths.getSharedPrefsFile( sharedPrefsName, context );
			if ( sharedPrefsFile.exists() )
			{
				sharedPrefsFile.delete();
			}
			else
			{
				IonLog.w( "Cache Index Store", "Clear: Shared Prefs File " + sharedPrefsFile.getPath() + " not found." );
			}
		}

		// unregister shared prefs
		metaPrefs.edit().clear().commit();
	}

	private static SharedPreferences getPrefs( IonConfig config, Context context )
	{
		return context.getSharedPreferences( getPrefKey( config ), 0 );
	}

	@NonNull
	private static String getPrefKey( IonConfig config )
	{
		return "cache_index_" + config.collectionIdentifier + "_" + config.locale + "_" + config.variation;
	}

	private static SharedPreferences getMetaPrefs( Context context )
	{
		return context.getSharedPreferences( "cache_index_meta", 0 );
	}
}