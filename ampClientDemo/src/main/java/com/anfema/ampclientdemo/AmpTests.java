package com.anfema.ampclientdemo;

import android.content.Context;

import com.anfema.ampclient.AmpAuthenticator;
import com.anfema.ampclient.AmpClient;
import com.anfema.ampclient.service.AmpApi;
import com.anfema.ampclient.service.AmpApiFactory;
import com.anfema.ampclient.models.Collection;
import com.anfema.ampclient.service.responses.CollectionResponse;
import com.anfema.ampclient.service.responses.LoginResponse;
import com.anfema.ampclient.models.Page;
import com.anfema.ampclient.service.responses.PageResponse;
import com.anfema.ampclient.utils.Log;
import com.anfema.ampclient.utils.RxDebugHooks;
import com.anfema.ampclient.utils.RxUtils;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class AmpTests
{
	private Context context;

	public AmpTests( Context appContext )
	{
		this.context = appContext;
	}

	public void execute()
	{
		RxDebugHooks.enableObservableHook();

		//		String baseUrl = context.getString( R.string.base_url );
		//		AmpAuthenticator.requestApiToken( baseUrl, "admin@anfe.ma", "test" )
		//				.subscribeOn( Schedulers.io() )
		//				.observeOn( AndroidSchedulers.mainThread() )
		//				.subscribe( Log::d, RxUtils.DEFAULT_EXCEPTION_HANDLER, () -> Log.d( "API token finished" ) );


		getAllPages();

		//		authenticateDirect();
	}

	private void authenticateConventional()
	{
		String baseUrl = context.getString( R.string.base_url );
		AmpAuthenticator.requestApiTokenConventional( baseUrl, "admin@anfe.ma", "test", ( apiToken, responseCode ) -> {
					if ( apiToken != null )
					{
						String collectionIdentifier = context.getString( R.string.collection_identifier );
						AmpClient ampClient = AmpClient.getInstance( context );
						ampClient.init( baseUrl, apiToken, collectionIdentifier );
						ampClient.getAllPages()
								.subscribe( page -> {/* process a page*/}, RxUtils.IGNORE_ERROR, () -> Log.d( "All pages downloaded!" ) );
					}
				}

		);
	}

	private Observable<AmpClient> getInitializedAmpClient()
	{
		String baseUrl = context.getString( R.string.base_url );
		String collectionIdentifier = context.getString( R.string.collection_identifier );
		AmpClient ampClient = AmpClient.getInstance( context );

		return AmpAuthenticator.requestApiToken( baseUrl, "admin@anfe.ma", "test" )
				.map( apiToken -> {
					Log.d( "API Token", apiToken );
					if ( apiToken == null )
					{
						Log.e( "API token is null" );
					}
					return apiToken;
				} )// TODO throw exception if api Token == null?
				.filter( apiToken -> apiToken != null )
				.map( apiToken -> ampClient.init( baseUrl, apiToken, collectionIdentifier ) );
	}

	private void getAllPages()
	{
		getInitializedAmpClient()
				.flatMap( AmpClient::getAllPages )
				.observeOn( AndroidSchedulers.mainThread() )
				.subscribe( page -> Log.d( page.toString() ), RxUtils.IGNORE_ERROR, () -> Log.d( "All pages downloaded" ) );
	}

	private void getFirstPage( String baseUrl, String apiToken )
	{
		String collectionIdentifier = context.getString( R.string.collection_identifier );
		AmpClient ampClient = AmpClient.getInstance( context )
				.init( baseUrl, apiToken, collectionIdentifier );
		// get first page
		ampClient.getCollection()
				.map( Collection::getPages )
				.flatMap( Observable::from )
				.map( page -> page.identifier )
				.flatMap( ampClient::getPage )
				.map( Page::toString )
				.subscribe( Log::d );
	}

	private void authenticateDirect()
	{
		final AmpApi ampApi = AmpApiFactory.newInstance( context.getString( R.string.base_url ) );

		final Call<LoginResponse> authenticateCall = ampApi.authenticateConventional( "admin@anfe.ma", "test" );
		authenticateCall.enqueue( new Callback<LoginResponse>()
		{
			@Override
			public void onResponse( Response<LoginResponse> loginResponse, Retrofit retrofit )
			{
				if ( loginResponse.isSuccess() )
				{
					loadCollectionDirect( loginResponse, ampApi );
				}
				else
				{
					Log.e( "Unsuccessful Request", "... returned code " + loginResponse.code() );
				}
			}

			@Override
			public void onFailure( Throwable t )
			{
				Log.d( "Amp Client", "Failure in login call" );
				Log.ex( t );
			}
		} );
	}


	void loadCollectionDirect( Response<LoginResponse> loginResponse, final AmpApi ampApi )
	{
		final String collectionIdentifier = context.getString( R.string.collection_identifier );
		final String apiToken = "Token " + loginResponse.body().getToken();
		final Call<CollectionResponse> collectionsCall = ampApi.getCollectionConventional( collectionIdentifier, apiToken );
		collectionsCall.enqueue( new Callback<CollectionResponse>()
		{
			@Override
			public void onResponse( Response<CollectionResponse> collectionsResponse, Retrofit retrofit )
			{
				if ( collectionsResponse.isSuccess() )
				{
					Collection collection = collectionsResponse.body().getCollection();
					// get first page
					String pageIdentifier = collection.getPages().get( 0 ).identifier;
					loadPageDirect( pageIdentifier, ampApi, collectionIdentifier, apiToken );

				}
				else
				{
					Log.e( "Unsuccessful Request", "... returned code " + collectionsResponse.code() );
				}
			}

			@Override
			public void onFailure( Throwable t )
			{
				Log.d( "Amp Client", "Failure in collections call" );
				Log.ex( t );
			}
		} );
	}

	void loadPageDirect( String pageIdentifier, AmpApi ampApi, String collectionIdentifier, String apiToken )
	{
		Call<PageResponse> pagesCall = ampApi.getPageConventional( collectionIdentifier, pageIdentifier, apiToken );
		pagesCall.enqueue( new Callback<PageResponse>()
		{
			@Override
			public void onResponse( Response<PageResponse> pagesResponse, Retrofit retrofit )
			{
				if ( pagesResponse.isSuccess() )
				{
					final Page page = pagesResponse.body().getPage();
					Log.d( "Amp Client", page.toString() );
				}
				else
				{
					Log.e( "Unsuccessful Request", "... returned code " + pagesResponse.code() );
				}
			}

			@Override
			public void onFailure( Throwable t )
			{
				Log.d( "Amp Client", "Failure in pages call" );
				Log.ex( t );
			}
		} );
	}

	//	private void loadCollection2( Response<LoginResponse> loginResponse, final AmpApi client )
	//	{
	//		final String collectionIdentifier = getString( R.string.collection_identifier );
	//		final String apiToken = "Token " + loginResponse.body().getToken();
	//		Observable<CollectionsResponse> collectionObservable = client.getCollection( collectionIdentifier, apiToken );
	//		collectionObservable.( new Callback<CollectionsResponse>()
	//		{
	//			@Override
	//			public void onResponse( Response<CollectionsResponse> collectionsResponse, Retrofit retrofit )
	//			{
	//				if ( collectionsResponse.isSuccess() )
	//				{
	//					Collection collection = collectionsResponse.body().getCollectionConventional();
	//					// get first page
	//					String pageIdentifier = collection.getPages().get( 0 ).identifier;
	//					loadPageDirect( pageIdentifier, client, collectionIdentifier, apiToken );
	//
	//				}
	//				else
	//				{
	//					Log.e( "Unsuccessful Request", "... returned code " + collectionsResponse.code() );
	//				}
	//			}
	//
	//			@Override
	//			public void onFailure( Throwable t )
	//			{
	//				Log.d( "Amp Client", "Failure in collections call" );
	//				Log.ex( t );
	//			}
	//		} );
	//	}
}