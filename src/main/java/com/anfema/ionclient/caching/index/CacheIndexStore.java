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
import com.anfema.utils.StringUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This is the global caching index table.
 * Single entries (either for collections, pages, or files) are stored in shared preferences.
 * There is a separate shared preferences instance for each collection.
 * An entry in shared preferences consist of the key, which is the URL the file has been requested from,
 * and the value, which is a subclass of {@link CacheIndex}.
 */
public class CacheIndexStore
{
	public static <T extends CacheIndex> T retrieve( String requestUrl, Class<T> cacheIndexSubclass, IonConfig config, Context context )
	{
		// check shared preferences
		IonLog.d( "Index Lookup", requestUrl + " from shared preferences" );
		SharedPreferences prefs = getPrefs( config, context );
		String json = prefs.getString( requestUrl, null );
		T index = GsonHolder.getInstance().fromJson( json, cacheIndexSubclass );

		if ( index != null )
		{
			// make cache index aware of its size by storing byte count to its field
			index.byteCount = ( int ) StringUtils.byteCount( json );
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
				// make cache index aware of its size by storing byte count to its field
				String indexSerialized = GsonHolder.getInstance().toJson( cacheIndex );
				cacheIndex.byteCount = ( int ) StringUtils.byteCount( indexSerialized );

				// save to shared preferences
				getPrefs( config, context )
						.edit()
						.putString( requestUrl, indexSerialized )
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

	public static void delete( String requestUrl, IonConfig config, Context context )
	{
		IonLog.d( "Cache Index", "deleting index for " + requestUrl );

		// delete from shared preferences
		getPrefs( config, context )
				.edit()
				.remove( requestUrl )
				.apply();
	}

	/**
	 * @param config to determine collection
	 * @return all index entry URLs of collection
	 */
	public static Set<String> retrieveAllUrls( IonConfig config, Context context )
	{
		// check shared preferences
		IonLog.d( "Cache Index", "Retrieve all index entries of collection " + config.collectionIdentifier );
		SharedPreferences prefs = getPrefs( config, context );

		Set<String> urls = new HashSet<>();
		for ( Entry<String, ?> entry : prefs.getAll().entrySet() )
		{
			urls.add( entry.getKey() );
		}
		return urls;
	}

	/**
	 * clear entire cache index in memory and file cache for a specific collection defined through {@param config}
	 */
	@SuppressLint("CommitPrefEdits")
	public static void clearCollection( IonConfig config, Context context )
	{
		// clear shared preferences - shared prefs file is still going to exist
		getPrefs( config, context ).edit().clear().apply();

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
		// clear all shared preferences
		SharedPreferences metaPrefs = getMetaPrefs( context );
		Map<String, ?> allPrefs = metaPrefs.getAll();
		for ( Map.Entry<String, ?> entry : allPrefs.entrySet() )
		{
			String sharedPrefsName = entry.getKey();
			// clear preferences
			context.getSharedPreferences( sharedPrefsName, 0 ).edit().clear().apply();
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
		metaPrefs.edit().clear().apply();
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