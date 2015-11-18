package com.anfema.ampclient;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anfema.ampclient.exceptions.AmpClientConfigInstantiateException;
import com.anfema.ampclient.models.Collection;
import com.anfema.ampclient.models.Page;
import com.anfema.ampclient.models.responses.CollectionResponse;
import com.anfema.ampclient.models.responses.PageResponse;
import com.anfema.ampclient.service.AmpApiFactory;
import com.anfema.ampclient.service.AmpApiRx;
import com.anfema.ampclient.service.HttpLoggingInterceptor;
import com.anfema.ampclient.utils.Log;
import com.anfema.ampclient.utils.RxUtils;
import com.squareup.okhttp.Interceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;

public class AmpClient implements AmpClientApi
{
	/// Multiton

	private static Map<Class<? extends AmpClientConfig>, AmpClient> instances = new HashMap<>();

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

		// At this point the client is initialized, but might not have an API token yet.

		return client.getInstanceWithAuthToken( appContext );
	}

	/**
	 * Asynchronously equests API token if not available yet.
	 *
	 * @return Observable emitting initialized client having an API token
	 */
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

	private Context         appContext;
	private AmpClientConfig ampClientConfig;
	private AmpApiRx        ampApi;
	private String          authHeaderValue;

	private AmpClient( Class<? extends AmpClientConfig> configClass, Context appContext ) throws AmpClientConfigInstantiateException
	{
		this.appContext = appContext;

		//noinspection TryWithIdenticalCatches
		try
		{
			ampClientConfig = configClass.newInstance();
			List<Interceptor> interceptors = new ArrayList<>();
			interceptors.add( new HttpLoggingInterceptor( "Retrofit Request" ) );
			ampApi = AmpApiFactory.newInstance( ampClientConfig.getBaseUrl( appContext ), interceptors, AmpApiRx.class );

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
	 * Update API token
	 */
	private void setAuthHeaderValue( String apiToken )
	{
		authHeaderValue = "token " + apiToken;
	}

	/// configuration END

	/// API Interface

	/**
	 * Add collection identifier and authorization token to request
	 */
	@Override
	public Observable<Collection> getCollection( String collectionIdentifier )
	{
		Log.d( Log.DEFAULT_TAG, "Requesting collections with collection identifier: " + collectionIdentifier + ", auth header value: " + authHeaderValue );
		return ampApi.getCollection( collectionIdentifier, authHeaderValue )
				.doOnNext( o -> Log.d( "****** Amp Client", "Received collection response" ) )
				.map( CollectionResponse::getCollection )
						//				.doOnNext( storeCollection() )
				.compose( RxUtils.applySchedulers() );
	}

	/**
	 * Add collection identifier and authorization token to request.<br/>
	 * Use default collection identifier as specified in {@link AmpClientConfig#getCollectionIdentifier(Context)}
	 */
	@Override
	public Observable<Collection> getCollection()
	{
		return getCollection( ampClientConfig.getCollectionIdentifier( appContext ) );
	}

	/**
	 * Add collection identifier and authorization token to request
	 */
	@Override
	public Observable<Page> getPage( String collectionIdentifier, String pageIdentifier )
	{
		Log.d( Log.DEFAULT_TAG, "Requesting page with collection identifier: " + collectionIdentifier + ", page identifier: " + pageIdentifier + ", auth header value: " + authHeaderValue );
		return ampApi.getPage( ampClientConfig.getCollectionIdentifier( appContext ), pageIdentifier, authHeaderValue )
				.map( PageResponse::getPage )
				.compose( RxUtils.applySchedulers() );
	}

	/**
	 * Add collection identifier and authorization token to request.<br/>
	 * Use default collection identifier as specified in {@link AmpClientConfig#getCollectionIdentifier(Context)}
	 */
	@Override
	public Observable<Page> getPage( String pageIdentifier )
	{
		return getPage( ampClientConfig.getCollectionIdentifier( appContext ), pageIdentifier );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events
	 */
	@Override
	public Observable<Page> getAllPages( String collectionIdentifier )
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

	/**
	 * A set of pages is "returned" by emitting multiple events.<br/>
	 * Use default collection identifier as specified in {@link AmpClientConfig#getCollectionIdentifier(Context)}
	 */
	@Override
	public Observable<Page> getAllPages()
	{
		return getAllPages( ampClientConfig.getCollectionIdentifier( appContext ) );
	}

	/// API Interface END
	// FIXME this is only a hack
	public static AmpClient getInstanceHack( Class<? extends AmpClientConfig> configClass, Context appContext )
	{
		return instances.get( configClass );
	}
}
