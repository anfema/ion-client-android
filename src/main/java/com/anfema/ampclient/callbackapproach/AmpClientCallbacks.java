package com.anfema.ampclient.callbackapproach;

import android.content.Context;

import com.anfema.ampclient.models.responses.CollectionResponse;
import com.anfema.ampclient.models.responses.PageResponse;
import com.anfema.ampclient.utils.ContextUtils;

import retrofit.Call;

public class AmpClientCallbacks implements AmpClientApiCallbacks
{
	/// Singleton

	private static AmpClientCallbacks instance;

	public static AmpClientCallbacks getInstance( Context context )
	{
		context = ContextUtils.getApplicationContext( context );

		if ( instance == null )
		{
			instance = new AmpClientCallbacks( context );
		}
		// application context can become null
		if ( instance.context == null )
		{
			instance.context = context;
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
		config = new AmpClientConfigCallbacks( context, baseUrl, apiToken, collectionIdentifier );
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


	private Context                  context;
	private AmpClientConfigCallbacks config;

	private AmpClientCallbacks( Context context )
	{
		this.context = context;
	}

	public AmpClientConfigCallbacks getConfig()
	{
		if ( config == null )
		{
			config = new AmpClientConfigCallbacks( context );
		}
		return config;
	}
}
