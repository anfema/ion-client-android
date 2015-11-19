package com.anfema.ampclient;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anfema.ampclient.exceptions.AmpClientConfigInstantiateException;
import com.anfema.ampclient.models.Collection;
import com.anfema.ampclient.models.Page;
import com.anfema.ampclient.models.PagePreview;
import com.anfema.ampclient.models.responses.CollectionResponse;
import com.anfema.ampclient.models.responses.PageResponse;
import com.anfema.ampclient.service.AmpApiFactory;
import com.anfema.ampclient.service.AmpApiRx;
import com.anfema.ampclient.service.AmpRequestLogger;
import com.anfema.ampclient.utils.Log;
import com.anfema.ampclient.utils.RxUtils;
import com.squareup.okhttp.Interceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Func1;

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
					.doOnNext( this::updateApiToken )
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
			interceptors.add( new AmpRequestLogger( "Retrofit Request" ) );
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

	private void updateApiToken( String apiToken )
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
		return ampApi.getCollection( collectionIdentifier, authHeaderValue )
				.map( CollectionResponse::getCollection )
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
		return getCollection( collectionIdentifier )
				.map( collection -> collection.pages )
				.flatMap( Observable::from )
				.map( page -> page.identifier )
				.flatMap( this::getPage );
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

	public Observable<Page> getSomePages( String collectionIdentifier, Func1<PagePreview, Boolean> pagesFilter )
	{
		return getCollection( collectionIdentifier )
				.map( collection -> collection.pages )
				.flatMap( Observable::from )
				.filter( pagesFilter )
				.map( page -> page.identifier )
				.flatMap( this::getPage );
	}

	/**
	 * A set of pages is "returned" by emitting multiple events.<br/>
	 * Use default collection identifier as specified in {@link AmpClientConfig#getCollectionIdentifier(Context)}
	 */
	public Observable<Page> getSomePages( Func1<PagePreview, Boolean> pagesFilter )
	{
		return getSomePages( ampClientConfig.getCollectionIdentifier( appContext ), pagesFilter );
	}


	/// API Interface END
}
