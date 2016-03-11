package com.anfema.ionclient;

import android.content.Context;

import com.anfema.ionclient.exceptions.IonConfigInvalidException;

import java.util.Arrays;

public class IonConfig
{
	public static final String DEFAULT_VARIATION                        = "default";
	public static final int    DEFAULT_MINUTES_UNTIL_COLLECTION_REFETCH = 5;

	// TODO add caching strategies: normal & strict-offline
	/**
	 * How many pages are kept in LRU memory cache? (for all client instances accumulated), unit: no. of page entries
	 */
	public static int pagesMemCacheSize = 100;

	/**
	 * base URL pointing to the ION endpoint
	 */
	public final String baseUrl;

	/**
	 * the collection identifier, {@link IonClient} will use for its calls
	 */
	public final String collectionIdentifier;

	/**
	 * authorization header value which is required to use the ION API
	 */
	public final String authorizationHeaderValue;

	/**
	 * Which language shall be requested? (e.g. "de_DE")
	 */
	public final String locale;

	/**
	 * For which platform/resolution are pages requested?
	 */
	public final String variation;

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
	public IonConfig( String baseUrl, String collectionIdentifier, String authorizationHeaderValue, String locale, String variation, boolean archiveDownloads, boolean ftsDbDownloads, int minutesUntilCollectionRefetch )
	{
		this.baseUrl = baseUrl;
		this.collectionIdentifier = collectionIdentifier;
		this.authorizationHeaderValue = authorizationHeaderValue;
		this.locale = locale;
		this.variation = variation;
		this.archiveDownloads = archiveDownloads;
		this.ftsDbDownloads = ftsDbDownloads;
		this.minutesUntilCollectionRefetch = minutesUntilCollectionRefetch;
	}

	/**
	 * Config constructor taking default values for {@link #minutesUntilCollectionRefetch}.
	 *
	 * @param variation Set a variation - other than default
	 */
	public IonConfig( String baseUrl, String collectionIdentifier, String authorizationHeaderValue, String locale, String variation, boolean archiveDownloads, boolean ftsDbDownloads )
	{
		this( baseUrl, collectionIdentifier, authorizationHeaderValue, locale, variation, archiveDownloads, ftsDbDownloads, DEFAULT_MINUTES_UNTIL_COLLECTION_REFETCH );
	}

	/**
	 * Config constructor taking default values for {@link #variation} and {@link #minutesUntilCollectionRefetch}..
	 */
	public IonConfig( String baseUrl, String collectionIdentifier, String authorizationHeaderValue, String locale, boolean archiveDownloads, boolean ftsDbDownloads )
	{
		this( baseUrl, collectionIdentifier, authorizationHeaderValue, locale, DEFAULT_VARIATION, archiveDownloads, ftsDbDownloads );
	}

	/**
	 * Config constructor taking default values for {@link #archiveDownloads}, {@link #ftsDbDownloads}, {@link #variation}, and {@link #minutesUntilCollectionRefetch}.
	 *
	 * @param context is required to determine the current device locale
	 */
	public IonConfig( String baseUrl, String collectionIdentifier, String authorizationHeaderValue, Context context )
	{
		this( baseUrl, collectionIdentifier, authorizationHeaderValue, context.getResources().getConfiguration().locale.toString(), DEFAULT_VARIATION, false, false );
	}

	public IonConfig( IonConfig otherConfig )
	{
		this.baseUrl = otherConfig.baseUrl;
		this.collectionIdentifier = otherConfig.collectionIdentifier;
		this.authorizationHeaderValue = otherConfig.authorizationHeaderValue;
		this.locale = otherConfig.locale;
		this.variation = otherConfig.variation;
		this.archiveDownloads = otherConfig.archiveDownloads;
		this.ftsDbDownloads = otherConfig.ftsDbDownloads;
		this.minutesUntilCollectionRefetch = otherConfig.minutesUntilCollectionRefetch;
	}

	public boolean isValid()
	{
		return baseUrl != null && baseUrl.contains( "://" )
				&& collectionIdentifier != null
				&& locale != null && locale.length() > 0
				&& authorizationHeaderValue != null && authorizationHeaderValue.length() > 0
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

	@Override
	public int hashCode()
	{
		Object[] hashRelevantFields = { baseUrl, collectionIdentifier, locale, variation };
		return Arrays.hashCode( hashRelevantFields );
	}
}
