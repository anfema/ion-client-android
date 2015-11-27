package com.anfema.ampclient;

import android.content.Context;

import rx.Observable;

public abstract class AmpClientConfig
{
	/**
	 * Implement to provide the base URL for the AMP endpoint.
	 * Must end with "/".
	 */
	public abstract String getBaseUrl( Context context );

	/**
	 * Implement to provide the collection identifier, which is defined for this app in AMP.
	 */
	public abstract String getCollectionIdentifier( Context context );

	/**
	 * Implement to provide an authorization token which is required to use the AMP API.<br/>
	 * This can be an API request. No caching needs to be handled here.<br/>
	 * This method expects an RxJava Observable.
	 */
	public abstract Observable<String> requestApiToken( Context context );
}
