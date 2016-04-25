package com.anfema.ionclient;

import android.content.Context;

import com.anfema.ionclient.exceptions.IonConfigInvalidException;
import com.anfema.ionclient.utils.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;

public class IonConfig
{
	public static final String DEFAULT_VARIATION                        = "default";
	public static final int    DEFAULT_MINUTES_UNTIL_COLLECTION_REFETCH = 5;

	/**
	 * Defines strategies, when to fetch data from cache and when to download it from internet.
	 */
	public enum CachingStrategy
	{
		/**
		 * strategy:
		 * 1. fetch current version from cache
		 * 2. download current version (if connected to internet)
		 * 3. fetch possibly outdated version from cache (if it exists)
		 * 4. error (because no version in cache exists and no internet connection)
		 */
		NORMAL,
		/**
		 * strategy:
		 * 1. fetch (possibly outdated) version from cache (if it exists)
		 * 2. error (because no version in cache exists and downloading is prohibited with this mode)
		 */
		STRICT_OFFLINE
	}

	// *** global configuration ***

	/**
	 * @see CachingStrategy
	 */
	public static CachingStrategy cachingStrategy = CachingStrategy.NORMAL;

	/**
	 * How many pages are kept in LRU memory cache? (for all client instances accumulated), unit: no. of page entries
	 */
	public static int pagesMemCacheSize = 100;


	private static Map<IonConfig, String> authorizations = new HashMap<>();


	// *** configuration of client instance ***

	/**
	 * base URL pointing to the ION endpoint
	 */
	public final String baseUrl;

	/**
	 * the collection identifier, {@link IonClient} will use for its calls
	 */
	public final String collectionIdentifier;

	/**
	 * Which language shall be requested? (e.g. "de_DE")
	 */
	public final String locale;

	/**
	 * For which platform/resolution are pages requested?
	 */
	public final String variation;

	/**
	 * Authorization header value is required to use the ION API. Primary option: provide it directly by passing the value.
	 */
	private String authorizationHeaderValue;

	/**
	 * Authorization header value is required to use the ION API. Secondary option: provide it indirectly through an async call.
	 */
	private final Observable<String> authorizationHeaderValueCall;

	/**
	 * Should the whole archive be downloaded when the collection is downloaded?
	 */
	public final boolean archiveDownloads;

	/**
	 * Should the full text search database be downloaded when the collection is downloaded?
	 */
	public final boolean ftsDbDownloads;

	/**
	 * Time after which collection is refreshed = fetched from server again.
	 */
	public final int minutesUntilCollectionRefetch;

	/**
	 * Default constructor with high degree of configuration possibilities.
	 */
	public IonConfig( String baseUrl, String collectionIdentifier, String locale, String variation, String authorizationHeaderValue, Observable<String> authorizationHeaderValueCall, boolean archiveDownloads, boolean ftsDbDownloads, int minutesUntilCollectionRefetch )
	{
		this.baseUrl = baseUrl;
		this.collectionIdentifier = collectionIdentifier;
		this.locale = locale;
		this.variation = variation;
		this.authorizationHeaderValue = authorizationHeaderValue;
		this.authorizationHeaderValueCall = authorizationHeaderValueCall;
		this.archiveDownloads = archiveDownloads;
		this.ftsDbDownloads = ftsDbDownloads;
		this.minutesUntilCollectionRefetch = minutesUntilCollectionRefetch;
	}

	/**
	 * Config constructor taking default values for {@link #minutesUntilCollectionRefetch}.
	 * Supports passing authentication calls.
	 *
	 * @param variation Set a variation - other than default
	 */
	public IonConfig( String baseUrl, String collectionIdentifier, String locale, String variation, String authorizationHeaderValue, Observable<String> authorizationHeaderValueCall, boolean archiveDownloads, boolean ftsDbDownloads )
	{
		this( baseUrl, collectionIdentifier, locale, variation, authorizationHeaderValue, authorizationHeaderValueCall, archiveDownloads, ftsDbDownloads, DEFAULT_MINUTES_UNTIL_COLLECTION_REFETCH );
	}

	/**
	 * Config constructor taking default values for {@link #variation} and {@link #minutesUntilCollectionRefetch}.
	 * Does not support passing authentication calls.
	 */
	public IonConfig( String baseUrl, String collectionIdentifier, String locale, String authorizationHeaderValue, boolean archiveDownloads, boolean ftsDbDownloads )
	{
		this( baseUrl, collectionIdentifier, locale, DEFAULT_VARIATION, authorizationHeaderValue, null, archiveDownloads, ftsDbDownloads );
	}

	/**
	 * Config constructor taking default values for {@link #archiveDownloads}, {@link #ftsDbDownloads}, {@link #variation}, and {@link #minutesUntilCollectionRefetch}.
	 * Does not support passing authentication calls.
	 *
	 * @param context is required to determine the current device locale
	 */
	public IonConfig( String baseUrl, String collectionIdentifier, String authorizationHeaderValue, Context context )
	{
		this( baseUrl, collectionIdentifier, context.getResources().getConfiguration().locale.toString(), authorizationHeaderValue, false, false );
	}

	public IonConfig( IonConfig otherConfig )
	{
		this.baseUrl = otherConfig.baseUrl;
		this.collectionIdentifier = otherConfig.collectionIdentifier;
		this.locale = otherConfig.locale;
		this.variation = otherConfig.variation;
		this.authorizationHeaderValue = otherConfig.authorizationHeaderValue;
		this.authorizationHeaderValueCall = otherConfig.authorizationHeaderValueCall;
		this.archiveDownloads = otherConfig.archiveDownloads;
		this.ftsDbDownloads = otherConfig.ftsDbDownloads;
		this.minutesUntilCollectionRefetch = otherConfig.minutesUntilCollectionRefetch;
	}

	public boolean isValid()
	{
		return baseUrl != null && baseUrl.contains( "://" )
				&& collectionIdentifier != null
				&& locale != null && locale.length() > 0
				&& minutesUntilCollectionRefetch >= 0
				&& pagesMemCacheSize >= 0;
	}

	public static void assertConfigIsValid( IonConfig config )
	{
		if ( config == null || !config.isValid() )
		{
			throw new IonConfigInvalidException();
		}
	}

	/**
	 * To check that attributes are equal which are ESSENTIAL for the IDENTITY of ION client
	 */
	@Override
	public boolean equals( Object obj )
	{
		if ( obj == this )
		{
			return true;
		}
		if ( !( obj instanceof IonConfig ) )
		{
			return false;
		}

		IonConfig other = ( IonConfig ) obj;
		return other.baseUrl.equals( baseUrl )
				&& other.collectionIdentifier.equals( collectionIdentifier )
				&& other.locale.equals( locale )
				&& other.variation.equals( variation );
	}

	/**
	 * To check that EVERY attribute is equal
	 */
	public boolean strictEquals( Object obj )
	{
		if ( !equals( obj ) )
		{
			return false;
		}
		IonConfig other = ( IonConfig ) obj;
		return other.authorizationHeaderValue.equals( authorizationHeaderValue )
				&& other.authorizationHeaderValueCall.equals( authorizationHeaderValueCall )
				&& other.archiveDownloads == archiveDownloads
				&& other.ftsDbDownloads == ftsDbDownloads
				&& other.minutesUntilCollectionRefetch == minutesUntilCollectionRefetch;
	}

	@Override
	public int hashCode()
	{
		Object[] hashRelevantFields = { baseUrl, collectionIdentifier, locale, variation };
		return Arrays.hashCode( hashRelevantFields );
	}

	public Observable<String> updateAuthorizationHeaderValue()
	{
		Log.w( "IonConfig", "UpdateAuthorization: authorizationHeaderValue != null: " + ( authorizationHeaderValue != null ) + ", call != null: " + ( authorizationHeaderValueCall != null ) );

		if ( authorizationHeaderValue != null || authorizationHeaderValueCall == null )
		{
			return Observable.just( authorizationHeaderValue );
		}

		String authorizationFromCache = authorizations.get( this );
		if ( authorizationFromCache != null )
		{
			return Observable.just( authorizationFromCache );
		}

		return authorizationHeaderValueCall
				.map( authorizationHeaderValue -> {
					authorizations.put( IonConfig.this, authorizationHeaderValue );
					this.authorizationHeaderValue = authorizationHeaderValue;
					return authorizationHeaderValue;
				} );
	}

	public String getAuthorizationHeaderValue()
	{
		if ( authorizationHeaderValue == null && authorizationHeaderValueCall != null )
		{
			Log.i( "IonConfig", "lookup in authorizations" );
			authorizationHeaderValue = authorizations.get( this );
		}
		Log.i( "IonConfig", "getAuthorizationHeaderValue: " + authorizationHeaderValue );
		return authorizationHeaderValue;
	}
}
