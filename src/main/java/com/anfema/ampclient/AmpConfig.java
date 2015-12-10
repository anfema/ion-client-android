package com.anfema.ampclient;

public class AmpConfig
{
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
}
