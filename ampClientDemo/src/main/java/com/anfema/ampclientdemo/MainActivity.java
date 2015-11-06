package com.anfema.ampclientdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.anfema.ampclient.service.AmpApi;
import com.anfema.ampclient.service.AmpApiFactory;
import com.anfema.ampclient.service.models.Collection;
import com.anfema.ampclient.service.models.CollectionsResponse;
import com.anfema.ampclient.service.models.LoginResponse;
import com.anfema.ampclient.service.models.Page;
import com.anfema.ampclient.service.models.PagesResponse;
import com.anfema.ampclient.utils.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity
{

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		//		setContentView( R.layout.activity_main );

		final AmpApi client = AmpApiFactory.newInstance( getString( R.string.base_url ) );

		final Call<LoginResponse> authenticateCall = client.authenticate( "admin@anfe.ma", "test" );
		authenticateCall.enqueue( new Callback<LoginResponse>()
		{
			@Override
			public void onResponse( Response<LoginResponse> loginResponse, Retrofit retrofit )
			{
				if ( loginResponse.isSuccess() )
				{
					loadCollection( loginResponse, client );
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

	private void loadCollection( Response<LoginResponse> loginResponse, final AmpApi client )
	{
		final String collectionIdentifier = getString( R.string.collection_identifier );
		final String apiToken = "Token " + loginResponse.body().getToken();
		final Call<CollectionsResponse> collectionsCall = client.getCollection( collectionIdentifier, apiToken );
		collectionsCall.enqueue( new Callback<CollectionsResponse>()
		{
			@Override
			public void onResponse( Response<CollectionsResponse> collectionsResponse, Retrofit retrofit )
			{
				if ( collectionsResponse.isSuccess() )
				{
					Collection collection = collectionsResponse.body().getCollection();
					// get first page
					String pageIdentifier = collection.getPages().get( 0 ).identifier;
					loadPage( pageIdentifier, client, collectionIdentifier, apiToken );

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

	//	private void loadCollection2( Response<LoginResponse> loginResponse, final AmpApi client )
	//	{
	//		final String collectionIdentifier = getString( R.string.collection_identifier );
	//		final String apiToken = "Token " + loginResponse.body().getToken();
	//		Observable<CollectionsResponse> collectionObservable = client.getCollection2( collectionIdentifier, apiToken );
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
	//					loadPage( pageIdentifier, client, collectionIdentifier, apiToken );
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

	private void loadPage( String pageIdentifier, AmpApi client, String collectionIdentifier, String apiToken )
	{
		runOnUiThread( () -> {

		} );

		runOnUiThread( () -> Log.d( "Test" ) );

		Call<PagesResponse> pagesCall = client.getPage( collectionIdentifier, pageIdentifier, apiToken );
		pagesCall.enqueue( new Callback<PagesResponse>()
		{
			@Override
			public void onResponse( Response<PagesResponse> pagesResponse, Retrofit retrofit )
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
}
