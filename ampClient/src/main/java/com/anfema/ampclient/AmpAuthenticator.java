package com.anfema.ampclient;

import com.anfema.ampclient.service.AmpApi;
import com.anfema.ampclient.service.AmpApiFactory;
import com.anfema.ampclient.service.models.LoginResponse;
import com.anfema.ampclient.utils.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Log in and retrieve API token.
 */
public class AmpAuthenticator
{
	public interface TokenCallback
	{
		void onToken( String apiToken, int responseCode );
	}

	public static void requestApiToken( String baseUrl, String username, String password, TokenCallback tokenReceiver )
	{
		AmpApi ampApi = AmpApiFactory.newInstance( baseUrl );
		Call<LoginResponse> loginResponseCall = ampApi.authenticate( username, password );
		loginResponseCall.enqueue( new Callback<LoginResponse>()
		{
			@Override
			public void onResponse( Response<LoginResponse> response, Retrofit retrofit )
			{
				if ( response.isSuccess() )
				{
					String apiToken = response.body().getToken();
					tokenReceiver.onToken( apiToken, response.code() );
				}
				else
				{
					tokenReceiver.onToken( null, response.code() );
				}
			}

			@Override
			public void onFailure( Throwable t )
			{
				tokenReceiver.onToken( null, -1 );
				Log.e( "Something went wrong when on authentication request." );
				Log.ex( t );
			}
		} );
	}
}
