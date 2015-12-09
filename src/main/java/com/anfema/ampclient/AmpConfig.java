package com.anfema.ampclient;

public class AmpConfig
{
	/**
	 * base URL pointing to the AMP endpoint
	 */
	public String baseUrl;

	/**
	 * the (default) collection identifier, which is defined for this app in AMP
	 */
	public String defaultCollectionIdentifier;

	/**
	 * authorization header value which is required to use the AMP API
	 */
	public String authorizationHeaderValue;

	public AmpConfig( String baseUrl, String defaultCollectionIdentifier, String authorizationHeaderValue )
	{
		this.baseUrl = baseUrl;
		this.defaultCollectionIdentifier = defaultCollectionIdentifier;
		this.authorizationHeaderValue = authorizationHeaderValue;
	}

	public AmpConfig( AmpConfig otherConfig )
	{
		this.baseUrl = otherConfig.baseUrl;
		this.defaultCollectionIdentifier = otherConfig.defaultCollectionIdentifier;
		this.authorizationHeaderValue = otherConfig.authorizationHeaderValue;
	}

	public boolean isValid()
	{
		return baseUrl != null && baseUrl.contains( "://" )
				&& defaultCollectionIdentifier != null
				&& authorizationHeaderValue != null && authorizationHeaderValue.length() > 0;
	}
}
