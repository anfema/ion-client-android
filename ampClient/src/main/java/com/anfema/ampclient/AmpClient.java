package com.anfema.ampclient;

import android.content.Context;

import com.anfema.ampclient.service.models.CollectionResponse;
import com.anfema.ampclient.service.models.PageResponse;

import retrofit.Call;
import rx.Observable;

public class AmpClient implements AmpClientAPI
{
	/// Singleton
	private static AmpClient instance;

	public static AmpClient getInstance( Context appContext )
	{
		if ( instance == null )
		{
			instance = new AmpClient( appContext );
		}
		// application context can become null
		if ( instance.appContext == null )
		{
			instance.appContext = appContext;
		}
		return instance;
	}
	/// Singleton END

	/**
	 * mandatory to initialize the client!
	 */
	public void init( String baseUrl, String authorizationToken, String collectionIdentifier )
	{
		config = new AmpClientConfig( appContext, baseUrl, authorizationToken, collectionIdentifier );
	}

	/// API Interface

	/**
	 * add collection identifier and authorization token to request
	 */
	@Override
	public Call<CollectionResponse> getCollection()
	{
		AmpClientConfig config = getConfig();
		return config.getAmpApi().getCollection( config.getCollectionIdentifier(), config.getApiToken() );
	}

	/**
	 * add collection identifier and authorization token to request
	 */
	@Override
	public Call<PageResponse> getPage( String pageIdentifier )
	{
		AmpClientConfig config = getConfig();
		return config.getAmpApi().getPage( config.getCollectionIdentifier(), pageIdentifier, config.getApiToken() );
	}

	/**
	 * add collection identifier and authorization token to request
	 */
	@Override
	public Observable<CollectionResponse> getCollectionRx()
	{
		AmpClientConfig config = getConfig();
		return config.getAmpApi().getCollectionRx( config.getCollectionIdentifier(), config.getApiToken() );
	}

	/**
	 * add collection identifier and authorization token to request
	 */
	@Override
	public Observable<PageResponse> getPageRx( String pageIdentifier )
	{
		AmpClientConfig config = getConfig();
		return config.getAmpApi().getPageRx( config.getCollectionIdentifier(), pageIdentifier, config.getApiToken() );
	}

	// TODO getAllPages

	/// API Interface END


	private Context         appContext;
	private AmpClientConfig config;

	private AmpClient( Context appContext )
	{
		this.appContext = appContext;
	}

	public AmpClientConfig getConfig()
	{
		if ( config == null )
		{
			config = new AmpClientConfig( appContext );
		}
		return config;
	}
}
