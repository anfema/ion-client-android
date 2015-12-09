package com.anfema.ampclient.authorization;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a useful helper for apps, that use more than one collection with each one requiring its own authorization.
 */
public class CollectionAuthStore
{
	private static final String PREFS_COLLECTION_AUTHORIZATION = "prefs_collection_authorization";

	/**
	 * key: collection identifier
	 * value: authorization header value
	 */
	private static Map<String, String> authorizations;

	/**
	 * Retrieve the authorization header value for collection identifier.
	 *
	 * @return authorization header value if entry exists, {@code null} otherwise
	 */
	public static String get( String collectionIdentifier, Context context )
	{
		if ( authorizations == null )
		{
			authorizations = new HashMap<>();
		}

		String authorization = authorizations.get( collectionIdentifier );
		if ( authorization != null )
		{
			return authorization;
		}

		// if not in memory cache, retrieve from shared preferences
		return getPrefs( context ).getString( collectionIdentifier, null );
	}

	/**
	 * Set authorization header value for a collection
	 */
	public static void set( String collectionIdentifier, String authorizationHeaderValue, Context context )
	{
		if ( authorizations == null )
		{
			authorizations = new HashMap<>();
		}

		// store to memory cache
		authorizations.put( collectionIdentifier, authorizationHeaderValue );

		// store to shared preferences
		SharedPreferences prefs = getPrefs( context );
		Editor editor = prefs.edit();
		editor.putString( collectionIdentifier, authorizationHeaderValue );
		editor.apply();
	}

	/**
	 * Convenience method if basic authentication is used.
	 */
	public static void set( String collectionIdentifier, String username, String password, Context context )
	{
		set( collectionIdentifier, BasicAuth.getAuthHeaderValue( username, password ), context );
	}

	private static SharedPreferences getPrefs( Context context )
	{
		return context.getSharedPreferences( PREFS_COLLECTION_AUTHORIZATION, 0 );
	}
}
