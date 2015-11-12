package com.anfema.ampclient.callbackapproach;

import android.content.Context;

import com.anfema.ampclient.models.responses.CollectionResponse;
import com.anfema.ampclient.models.responses.PageResponse;

import retrofit.Call;

public class AmpClientCallbacks implements AmpClientApiCallbacks
{
	/// Singleton

	private static AmpClientCallbacks instance;

	public static AmpClientCallbacks getInstance( Context appContext )
	{
		if ( instance == null )
		{
			instance = new AmpClientCallbacks( appContext );
		}
		// application context can become null
		if ( instance.appContext == null )
		{
			instance.appContext = appContext;
		}
		return instance;
	}
	/// Singleton END


	/// configuration

	/**
	 * It is mandatory to initialize the client!
	 */

	public AmpClientCallbacks init( String baseUrl, String apiToken, String collectionIdentifier )
	{
		config = new AmpClientConfigCallbacks( appContext, baseUrl, apiToken, collectionIdentifier );
		return this;
	}

	/**
	 * Update API token
	 */
	public void setApiTokenConventional( String apiToken )
	{
		getConfig().setApiToken( apiToken );
	}

	/// configuration END

	/// API Interface

	/**
	 * add collection identifier and authorization token to request
	 */
	@Override
	public Call<CollectionResponse> getCollectionConventional()
	{
		AmpClientConfigCallbacks config = getConfig();
		return config.getAmpApi().getCollectionConventional( config.getCollectionIdentifier(), config.getAuthHeaderValue() );
	}

	/**
	 * add collection identifier and authorization token to request
	 */
	@Override
	public Call<PageResponse> getPageConventional( String pageIdentifier )
	{
		AmpClientConfigCallbacks config = getConfig();
		return config.getAmpApi().getPageConventional( config.getCollectionIdentifier(), pageIdentifier, config.getAuthHeaderValue() );
	}

	/// API Interface END


	private Context                  appContext;
	private AmpClientConfigCallbacks config;

	private AmpClientCallbacks( Context appContext )
	{
		this.appContext = appContext;
	}

	public AmpClientConfigCallbacks getConfig()
	{
		if ( config == null )
		{
			config = new AmpClientConfigCallbacks( appContext );
		}
		return config;
	}
}
