package com.anfema.ampclient;

import android.content.Context;

import com.anfema.ampclient.service.models.Collection;
import com.anfema.ampclient.service.models.CollectionResponse;
import com.anfema.ampclient.service.models.Page;
import com.anfema.ampclient.service.models.PageResponse;
import com.anfema.ampclient.utils.RxUtils;

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

	/// configuration

	/**
	 * It is mandatory to initialize the client!
	 */
	public AmpClient init( String baseUrl, String apiToken, String collectionIdentifier )
	{
		config = new AmpClientConfig( appContext, baseUrl, apiToken, collectionIdentifier );
		return this;
	}

	/**
	 * Update API token
	 */
	public void setApiToken( String apiToken )
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
		AmpClientConfig config = getConfig();
		return config.getAmpApi().getCollectionConventional( config.getCollectionIdentifier(), config.getApiToken() );
	}

	/**
	 * add collection identifier and authorization token to request
	 */
	@Override
	public Call<PageResponse> getPageConventional( String pageIdentifier )
	{
		AmpClientConfig config = getConfig();
		return config.getAmpApi().getPageConventional( config.getCollectionIdentifier(), pageIdentifier, config.getApiToken() );
	}

	/**
	 * add collection identifier and authorization token to request
	 */
	@Override
	public Observable<Collection> getCollection()
	{
		AmpClientConfig config = getConfig();
		return config.getAmpApi().getCollection( config.getCollectionIdentifier(), config.getApiToken() )
				.map( CollectionResponse::getCollection )
				.doOnError( RxUtils.DEFAULT_EXCEPTION_HANDLER )
				.compose( RxUtils.applySchedulers() );
	}

	/**
	 * add collection identifier and authorization token to request
	 */
	@Override
	public Observable<Page> getPage( String pageIdentifier )
	{
		AmpClientConfig config = getConfig();
		return config.getAmpApi().getPage( config.getCollectionIdentifier(), pageIdentifier, config.getApiToken() )
				.map( PageResponse::getPage )
				.doOnError( RxUtils.DEFAULT_EXCEPTION_HANDLER )
				.compose( RxUtils.applySchedulers() );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.
	 */
	@Override
	public Observable<Page> getAllPages()
	{
		return getCollection()
				.map( Collection::getPages )
				.flatMap( Observable::from )
				.map( page -> page.identifier )
				.flatMap( this::getPage );
	}

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
