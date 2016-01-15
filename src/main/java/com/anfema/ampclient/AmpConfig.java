package com.anfema.ampclient;

import com.anfema.ampclient.exceptions.AmpConfigInvalidException;

import java.util.Arrays;

public class AmpConfig
{
	// TODO add variation?

	/**
	 * base URL pointing to the AMP endpoint
	 */
	public final String baseUrl;

	/**
	 * Which language shall be requested? (e.g. "de_DE")
	 */
	public final String locale;

	/**
	 * the collection identifier, {@link AmpClient} will use for its calls
	 */
	public final String collectionIdentifier;

	/**
	 * authorization header value which is required to use the AMP API
	 */
	public final String authorizationHeaderValue;

	/**
	 * Time after which collection is refreshed = fetched from server again.
	 */
	public final int minutesUntilCollectionRefetch;

	/**
	 * How many pages are kept in LRU memory cache?
	 */
	public final int pagesMemCacheSize;

	/**
	 * Should the whole archive be downloaded when the collection is downloaded?
	 */
	public final boolean archiveDownloads;

	public AmpConfig( String baseUrl, String locale, String collectionIdentifier, String authorizationHeaderValue, int minutesUntilCollectionRefetch, int pagesMemCacheSize, boolean archiveDownloads )
	{
		this.baseUrl = baseUrl;
		this.collectionIdentifier = collectionIdentifier;
		this.authorizationHeaderValue = authorizationHeaderValue;
		this.minutesUntilCollectionRefetch = minutesUntilCollectionRefetch;
		this.archiveDownloads = archiveDownloads;
		this.pagesMemCacheSize = pagesMemCacheSize;
		this.locale = locale;
	}

	public AmpConfig( AmpConfig otherConfig )
	{
		this.baseUrl = otherConfig.baseUrl;
		this.collectionIdentifier = otherConfig.collectionIdentifier;
		this.authorizationHeaderValue = otherConfig.authorizationHeaderValue;
		this.minutesUntilCollectionRefetch = otherConfig.minutesUntilCollectionRefetch;
		this.archiveDownloads = otherConfig.archiveDownloads;
		this.pagesMemCacheSize = otherConfig.pagesMemCacheSize;
		this.locale = otherConfig.locale;
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

	public static void assertConfigIsValid( AmpConfig config )
	{
		if ( config == null || !config.isValid() )
		{
			throw new AmpConfigInvalidException();
		}
	}

	@Override
	public boolean equals( Object obj )
	{
		if ( obj == this )
		{
			return true;
		}
		if ( obj == null )
		{
			return false;
		}
		if ( obj instanceof AmpConfig )
		{
			AmpConfig other = ( AmpConfig ) obj;
			return other.baseUrl.equals( baseUrl )
					&& other.locale.equals( locale )
					&& other.collectionIdentifier.equals( collectionIdentifier )
					&& other.authorizationHeaderValue.equals( authorizationHeaderValue )
					&& other.minutesUntilCollectionRefetch == minutesUntilCollectionRefetch
					&& other.archiveDownloads == archiveDownloads
					&& other.pagesMemCacheSize == pagesMemCacheSize;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		Object[] hashRelevantFields = { baseUrl, locale, collectionIdentifier, authorizationHeaderValue, minutesUntilCollectionRefetch, pagesMemCacheSize, archiveDownloads };
		return Arrays.hashCode( hashRelevantFields );
	}
}
