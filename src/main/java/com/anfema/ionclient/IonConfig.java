package com.anfema.ionclient;

import android.content.Context;

import com.anfema.ionclient.exceptions.IonConfigInvalidException;
import com.anfema.utils.EqualsContract;

import java.util.Arrays;

import androidx.annotation.NonNull;

public class IonConfig
{
	public static final String DEFAULT_VARIATION                        = "default";
	public static final int    DEFAULT_MINUTES_UNTIL_COLLECTION_REFETCH = 5;
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
	 * Should the whole archive be downloaded when the collection is downloaded?
	 */
	public final boolean archiveDownloads;

	/**
	 * Time after which collection is refreshed = fetched from server again.
	 */
	public final int minutesUntilCollectionRefetch;

	@SuppressWarnings("unused")
	public static class Builder
	{
		private final String  baseUrl;
		private final String  collectionIdentifier;
		private       String  locale;
		private       String  variation                     = DEFAULT_VARIATION;
		private       boolean archiveDownloads              = false;
		private       int     minutesUntilCollectionRefetch = DEFAULT_MINUTES_UNTIL_COLLECTION_REFETCH;


		public Builder( String baseUrl, String collectionIdentifier )
		{
			this.baseUrl = baseUrl;
			this.collectionIdentifier = collectionIdentifier;
		}

		public Builder locale( String locale )
		{
			this.locale = locale;
			return this;
		}

		/**
		 * Set locale from device configuration
		 */
		public Builder locale( Context context )
		{
			this.locale = context.getResources().getConfiguration().locale.toString();
			return this;
		}

		public Builder variation( String variation )
		{
			this.variation = variation;
			return this;
		}

		public Builder archiveDownloads( boolean archiveDownloads )
		{
			this.archiveDownloads = archiveDownloads;
			return this;
		}

		public Builder minutesUntilCollectionRefetch( int minutesUntilCollectionRefetch )
		{
			this.minutesUntilCollectionRefetch = minutesUntilCollectionRefetch;
			return this;
		}

		public IonConfig build()
		{
			IonConfig config = new IonConfig(
					baseUrl,
					collectionIdentifier,
					locale,
					variation,
					archiveDownloads,
					minutesUntilCollectionRefetch
			);
			assertConfigIsValid( config );
			return config;
		}
	}

	public IonConfig(
			String baseUrl,
			String collectionIdentifier,
			String locale,
			String variation,
			boolean archiveDownloads,
			int minutesUntilCollectionRefetch
	)
	{
		this.baseUrl = baseUrl;
		this.collectionIdentifier = collectionIdentifier;
		this.locale = locale;
		this.variation = variation;
		this.archiveDownloads = archiveDownloads;
		this.minutesUntilCollectionRefetch = minutesUntilCollectionRefetch;
	}

	public IonConfig( @NonNull IonConfig otherConfig )
	{
		this.baseUrl = otherConfig.baseUrl;
		this.collectionIdentifier = otherConfig.collectionIdentifier;
		this.locale = otherConfig.locale;
		this.variation = otherConfig.variation;
		this.archiveDownloads = otherConfig.archiveDownloads;
		this.minutesUntilCollectionRefetch = otherConfig.minutesUntilCollectionRefetch;
	}

	public boolean isValid()
	{
		return baseUrl != null && baseUrl.contains( "://" )
				&& collectionIdentifier != null
				&& locale != null && locale.length() > 0;
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
	public final boolean equals( Object obj )
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
		return EqualsContract.equal( baseUrl, other.baseUrl )
				&& EqualsContract.equal( collectionIdentifier, other.collectionIdentifier )
				&& EqualsContract.equal( locale, other.locale )
				&& EqualsContract.equal( variation, other.variation );
	}

	@Override
	public final int hashCode()
	{
		Object[] hashRelevantFields = { baseUrl, collectionIdentifier, locale, variation };
		return Arrays.hashCode( hashRelevantFields );
	}
}
