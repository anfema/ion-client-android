package com.anfema.ampclientdemo;

import android.content.Context;

import com.anfema.ampclient.AmpAuthenticator;
import com.anfema.ampclient.AmpClient;
import com.anfema.ampclient.service.AmpApi;
import com.anfema.ampclient.service.AmpApiFactory;
import com.anfema.ampclient.service.models.Collection;
import com.anfema.ampclient.service.models.CollectionResponse;
import com.anfema.ampclient.service.models.LoginResponse;
import com.anfema.ampclient.service.models.Page;
import com.anfema.ampclient.service.models.PageResponse;
import com.anfema.ampclient.utils.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class AmpTests
{

	private Context context;

	public AmpTests( Context appContext )
	{
		this.context = appContext;
	}

	public void execute()
	{
		String baseUrl = context.getString( R.string.base_url );
		AmpAuthenticator.requestApiToken( baseUrl, "admin@anfe.ma", "test", ( apiToken, responseCode ) -> {
			if ( apiToken != null )
			{
				AmpClient.getInstance();
				// TODO getFirstPage
			}
		} );

		authenticateDirect();
	}

	private void authenticateDirect()
	{
		final AmpApi ampApi = AmpApiFactory.newInstance( context.getString( R.string.base_url ) );

		final Call<LoginResponse> authenticateCall = ampApi.authenticate( "admin@anfe.ma", "test" );
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
		final Call<CollectionResponse> collectionsCall = ampApi.getCollection( collectionIdentifier, apiToken );
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
		Call<PageResponse> pagesCall = ampApi.getPage( collectionIdentifier, pageIdentifier, apiToken );
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
	//		Observable<CollectionsResponse> collectionObservable = client.getCollectionRx( collectionIdentifier, apiToken );
	//		collectionObservable.( new Callback<CollectionsResponse>()
	//		{
	//			@Override
	//			public void onResponse( Response<CollectionsResponse> collectionsResponse, Retrofit retrofit )
	//			{
	//				if ( collectionsResponse.isSuccess() )
	//				{
	//					Collection collection = collectionsResponse.body().getCollection();
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