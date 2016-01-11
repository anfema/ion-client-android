package com.anfema.ampclient;

import com.anfema.ampclient.exceptions.AmpConfigInvalidException;

public class AmpConfig
{
	// TODO add Locale, add variation?

	/**
	 * base URL pointing to the AMP endpoint
	 */
	public final String baseUrl;

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

	public AmpConfig( String baseUrl, String collectionIdentifier, String authorizationHeaderValue, int minutesUntilCollectionRefetch )
	{
		this.baseUrl = baseUrl;
		this.collectionIdentifier = collectionIdentifier;
		this.authorizationHeaderValue = authorizationHeaderValue;
		this.minutesUntilCollectionRefetch = minutesUntilCollectionRefetch;
	}

	public AmpConfig( AmpConfig otherConfig )
	{
		this.baseUrl = otherConfig.baseUrl;
		this.collectionIdentifier = otherConfig.collectionIdentifier;
		this.authorizationHeaderValue = otherConfig.authorizationHeaderValue;
		this.minutesUntilCollectionRefetch = otherConfig.minutesUntilCollectionRefetch;
	}

	public boolean isValid()
	{
		return baseUrl != null && baseUrl.contains( "://" )
				&& collectionIdentifier != null
				&& authorizationHeaderValue != null && authorizationHeaderValue.length() > 0;
	}

	public static void assertConfigIsValid( AmpConfig config )
	{
		if ( config == null || !config.isValid() )
		{
			throw new AmpConfigInvalidException();
		}
	}
}
