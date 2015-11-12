package com.anfema.ampclientdemo;

import android.content.Context;

import com.anfema.ampclient.callbackapproach.AmpAuthenticatorCallbacks;
import com.anfema.ampclient.callbackapproach.AmpClientCallbacks;
import com.anfema.ampclient.models.Collection;
import com.anfema.ampclient.models.PagePreview;
import com.anfema.ampclient.models.responses.CollectionResponse;
import com.anfema.ampclient.models.responses.PageResponse;
import com.anfema.ampclient.utils.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class AmpDemoCallbacks
{
	private Context appContext;
	private String  baseUrl;
	private String  collectionIdentifier;

	public AmpDemoCallbacks( Context appContext )
	{
		this.appContext = appContext;
		baseUrl = appContext.getString( R.string.base_url );
		collectionIdentifier = appContext.getString( R.string.collection_identifier );
	}

	public void execute()
	{
		// start first of callback containing methods
		authenticateConventional();
	}

	private void authenticateConventional()
	{
		AmpAuthenticatorCallbacks.requestApiTokenConventional( baseUrl, "admin@anfe.ma", "test", ( apiToken, responseCode ) -> {
					if ( apiToken != null )
					{
						getCollectionConventional( apiToken );
					}
				}
		);
	}

	private AmpClientCallbacks getInitializedAmpClient( String apiToken )
	{
		final String collectionIdentifier = appContext.getString( R.string.collection_identifier );
		final String authHeaderValue = "Token " + apiToken;

		AmpClientCallbacks ampClient = AmpClientCallbacks.getInstance( appContext );
		ampClient.init( baseUrl, authHeaderValue, collectionIdentifier );
		return ampClient;
	}

	private void getCollectionConventional( String apiToken )
	{
		AmpClientCallbacks ampClient = getInitializedAmpClient( apiToken );
		Call<CollectionResponse> collectionCall = ampClient.getCollectionConventional();
		collectionCall.enqueue( new Callback<CollectionResponse>()
		{
			@Override
			public void onResponse( Response<CollectionResponse> collectionResponse, Retrofit retrofit )
			{
				if ( !collectionResponse.isSuccess() )
				{
					Log.e( "Unsuccessful Request", "... returned code " + collectionResponse.code() );
					return;
				}

				getAllPages( collectionResponse, ampClient );
			}

			@Override
			public void onFailure( Throwable t )
			{
				Log.d( "Amp Client", "Failure in collections call" );
				Log.ex( t );
			}
		} );
	}

	private void getAllPages( Response<CollectionResponse> collectionResponse, AmpClientCallbacks ampClient )
	{
		Collection collection = collectionResponse.body().getCollection();
		for ( PagePreview pagePreview : collection.pages )
		{
			getPage( ampClient, pagePreview );
		}
	}

	private void getPage( AmpClientCallbacks ampClient, PagePreview pagePreview )
	{
		Call<PageResponse> pageCall = ampClient.getPageConventional( pagePreview.identifier );
		pageCall.enqueue( new Callback<PageResponse>()
		{
			@Override
			public void onResponse( Response<PageResponse> pageResponse, Retrofit retrofit )
			{
				if ( !pageResponse.isSuccess() )
				{
					Log.e( "Unsuccessful Request", "... returned code " + pageResponse.code() );
				}

				Log.d( "Amp Client", "Page downloaded: " + pageResponse.body().getPage().toString() );
			}

			@Override
			public void onFailure( Throwable t )
			{
				Log.d( "Amp Client", "Failure in collections call" );
				Log.ex( t );
			}
		} );
	}
}