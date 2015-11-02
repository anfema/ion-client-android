package com.anfema.ampclientdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.anfema.ampclient.AmpClientFactory;
import com.anfema.ampclient.service.AmpService;
import com.anfema.ampclient.service.response_gsons.Collection;
import com.anfema.ampclient.service.response_gsons.CollectionsResponse;
import com.anfema.ampclient.service.response_gsons.LoginResponse;
import com.anfema.ampclient.service.response_gsons.Page;
import com.anfema.ampclient.service.response_gsons.PagesResponse;

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

		final AmpService client = AmpClientFactory.createClient( getString( R.string.base_url ) );

		final Call<LoginResponse> authenticateCall = client.authenticate( "admin@anfe.ma", "test" );
		authenticateCall.enqueue( new Callback<LoginResponse>()
		{
			@Override
			public void onResponse( Response<LoginResponse> loginResponse, Retrofit retrofit )
			{
				if ( loginResponse.isSuccess() )
				{
					final String apiToken = "Token " + loginResponse.body().getToken();
					Call<PagesResponse> pagesCall = client.getPages( apiToken );
					pagesCall.enqueue( new Callback<PagesResponse>()
					{
						@Override
						public void onResponse( Response<PagesResponse> pagesResponse, Retrofit retrofit )
						{
							if ( pagesResponse.isSuccess() )
							{
								final Page[] pages = pagesResponse.body().getPages();

								final Call<CollectionsResponse> collectionsCall = client.getCollections( apiToken );
								collectionsCall.enqueue( new Callback<CollectionsResponse>()
								{
									@Override
									public void onResponse( Response<CollectionsResponse> collectionsResponse, Retrofit retrofit )
									{
										if ( collectionsResponse.isSuccess() )
										{
											Collection[] collections = collectionsResponse.body().getCollections();
											collections.toString();
										}
										else
										{
											Log.e( "Unsuccessful Request", "... returned code " + collectionsResponse.code() );
										}
									}

									@Override
									public void onFailure( Throwable t )
									{
										Log.d( "Amp Client", "Failure" );
									}
								} );

							}
							else
							{
								Log.e( "Unsuccessful Request", "... returned code " + pagesResponse.code() );
							}
						}

						@Override
						public void onFailure( Throwable t )
						{
							Log.d( "Amp Client", "Failure" );
						}
					} );
				}
				else
				{
					Log.e( "Unsuccessful Request", "... returned code " + loginResponse.code() );
				}
			}

			@Override
			public void onFailure( Throwable t )
			{
				Log.d( "Amp Client", "Failure" );
			}
		} );
	}
}
