package com.anfema.ampclient;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anfema.ampclient.models.Collection;
import com.anfema.ampclient.models.Page;
import com.anfema.ampclient.models.responses.CollectionResponse;
import com.anfema.ampclient.models.responses.PageResponse;
import com.anfema.ampclient.service.AmpApiFactory;
import com.anfema.ampclient.service.AmpApiRx;
import com.anfema.ampclient.exceptions.AmpClientConfigInstantiateException;
import com.anfema.ampclient.utils.Log;
import com.anfema.ampclient.utils.RxUtils;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;

public class AmpClient implements AmpClientApi
{
	/// Multiton

	private static Map<Class<? extends AmpClientConfig>, AmpClient> instances = new HashMap<>();
	private Context         appContext;
	private AmpClientConfig ampClientConfig;
	private AmpApiRx        ampApi;

	private AmpClient( Class<? extends AmpClientConfig> configClass, Context appContext ) throws AmpClientConfigInstantiateException
	{
		this.appContext = appContext;

		//noinspection TryWithIdenticalCatches
		try
		{
			ampClientConfig = configClass.newInstance();
			ampApi = AmpApiFactory.newInstance( ampClientConfig.getBaseUrl( appContext ), AmpApiRx.class );

			instances.put( configClass, this );
		}
		catch ( InstantiationException e )
		{
			throw new AmpClientConfigInstantiateException();
		}
		catch ( IllegalAccessException e )
		{
			throw new AmpClientConfigInstantiateException();
		}
	}

	/**
	 * @param configClass implementation of AmpClientConfig interface
	 * @param appContext
	 * @return client instance, ready to go (with API token set)
	 */
	public static Observable<AmpClient> getInstance( Class<? extends AmpClientConfig> configClass, Context appContext )
	{
		if ( instances == null )
		{
			instances = new HashMap<>();
		}

		// check if client for this configuration already exists, otherwise create an instance
		AmpClient client = instances.get( configClass );
		if ( client == null )
		{
			try
			{
				client = new AmpClient( configClass, appContext );
			}
			catch ( AmpClientConfigInstantiateException e )
			{
				return Observable.error( e );
			}
		}
		else if ( client.appContext == null )
		{
			// fail early if app context is null
			if ( appContext == null )
			{
				return Observable.error( new NullPointerException( "App context is null" ) );
			}
			// update appContext for existing clients if it became null
			client.appContext = appContext;
		}

		return client.getInstanceWithAuthToken( appContext );
	}

	@NonNull
	private Observable<AmpClient> getInstanceWithAuthToken( Context appContext )
	{
		Observable<AmpClient> clientObservable;
		if ( authHeaderValue != null /* || TODO offline mode */ )
		{
			// authHeaderValue is available
			clientObservable = Observable.just( this );
		}
		else
		{
			// need to retrieve API token
			final AmpClient finalClient = this;
			clientObservable = ampClientConfig.retrieveApiToken( appContext )
					.doOnNext( this::setAuthHeaderValue )
					.doOnNext( apiToken -> Log.d( "****** Amp Client ******", "received API token: " + apiToken ) )
					.map( apiToken -> finalClient );
		}
		return clientObservable
				.doOnError( RxUtils.DEFAULT_EXCEPTION_HANDLER );
	}

	/// Multiton END


	/// configuration

	public String authHeaderValue;

	/**
	 * Update API token
	 */
	private void setAuthHeaderValue( String apiToken )
	{
		authHeaderValue = "token " + apiToken;
	}

	/// configuration END

	/// API Interface

	/**
	 * add collection identifier and authorization token to request
	 */
	@Override
	public Observable<Collection> getCollection()
	{
		Log.d( "***** Amp Client *****", "Auth header value: " + authHeaderValue );
		return ampApi.getCollection( ampClientConfig.getCollectionIdentifier( appContext ), authHeaderValue )
				.doOnNext( o -> Log.d( "****** Amp Client", "Received collection response" ) )
				.map( CollectionResponse::getCollection )
				.compose( RxUtils.applySchedulers() );
	}

	/**
	 * add collection identifier and authorization token to request
	 */
	@Override
	public Observable<Page> getPage( String pageIdentifier )
	{
		return ampApi.getPage( ampClientConfig.getCollectionIdentifier( appContext ), pageIdentifier, authHeaderValue )
				.map( PageResponse::getPage )
				.compose( RxUtils.applySchedulers() );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.
	 */
	@Override
	public Observable<Page> getAllPages()
	{
		return getCollection()
				.doOnNext( collection1 -> Log.d( "received collection" ) )
				.map( collection -> collection.pages )
				.doOnNext( collection -> Log.d( "mapping1: col pages" ) )
				.flatMap( Observable::from )
				.doOnNext( collection -> Log.d( "split col page" ) )
				.map( page -> page.identifier )
				.doOnNext( collection -> Log.d( "mapping2: page_identifier" ) )
				.flatMap( this::getPage )
				.doOnNext( collection -> Log.d( "received pages" ) );
	}

	/// API Interface END
}
