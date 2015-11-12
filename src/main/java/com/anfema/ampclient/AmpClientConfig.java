package com.anfema.ampclient;

import android.content.Context;

import rx.Observable;

public abstract class AmpClientConfig
{
	// keys for shared preferences
	public static final String PREFS_NAME      = "prefs_amp_client";
	public static final String PREFS_API_TOKEN = "prefs_api_token";

	/**
	 * Implement to provide the base URL for the AMP endpoint.
	 */
	public abstract String getBaseUrl( Context appContext );

	/**
	 * Implement to provide the collection identifier, which is defined for this app in AMP.
	 */
	public abstract String getCollectionIdentifier( Context appContext );

	/**
	 * Implement to provide an authorization token which is required to use the AMP API.<br/>
	 * This can be an API request. No caching needs to be handled here.<br/>
	 * This method expects an RxJava Observable.
	 */
	public abstract Observable<String> requestApiToken( Context appContext );

	/**
	 * This method wraps {@link #requestApiToken(Context)}. It retrieves API token from cache if available.
	 */
	public Observable<String> retrieveApiToken( Context appContext )
	{
		/*
		// look up in local storage
		SharedPreferences prefs = appContext.getSharedPreferences( PREFS_NAME, 0 );
		String localToken = prefs.getString( PREFS_API_TOKEN, null );
		if ( localToken != null )
		{
			return Observable.just( localToken );
		}
		*/

		// fetch token via provided api Token call
		return requestApiToken( appContext )
				/*.doOnNext( apiToken -> {
					Editor editor = prefs.edit();
					editor.putString( PREFS_API_TOKEN, apiToken ).apply();
				} )*/;
	}
}
