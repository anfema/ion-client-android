package com.anfema.ampclient;

import com.anfema.ampclient.exceptions.AmpConfigInvalidException;

public class AmpConfig
{
	// TODO add Locale, add variation?

	/**
	 * base URL pointing to the AMP endpoint
	 */
	public String baseUrl;

	/**
	 * the collection identifier, {@link AmpClient} will use for its calls
	 */
	public String collectionIdentifier;

	/**
	 * authorization header value which is required to use the AMP API
	 */
	public String authorizationHeaderValue;

	public AmpConfig( String baseUrl, String collectionIdentifier, String authorizationHeaderValue )
	{
		this.baseUrl = baseUrl;
		this.collectionIdentifier = collectionIdentifier;
		this.authorizationHeaderValue = authorizationHeaderValue;
	}

	public AmpConfig( AmpConfig otherConfig )
	{
		this.baseUrl = otherConfig.baseUrl;
		this.collectionIdentifier = otherConfig.collectionIdentifier;
		this.authorizationHeaderValue = otherConfig.authorizationHeaderValue;
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
