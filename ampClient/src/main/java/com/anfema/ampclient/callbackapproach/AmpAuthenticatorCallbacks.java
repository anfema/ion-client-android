package com.anfema.ampclient.callbackapproach;

import com.anfema.ampclient.models.responses.LoginResponse;
import com.anfema.ampclient.service.AmpApiCallbacks;
import com.anfema.ampclient.service.AmpApiFactory;
import com.anfema.ampclient.utils.Log;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Use AMPs internal login request and retrieve API token.
 */
public class AmpAuthenticatorCallbacks
{
	public interface TokenCallback
	{
		void onToken( String apiToken, int responseCode );
	}

	public static void requestApiTokenConventional( String baseUrl, String username, String password, TokenCallback tokenReceiver )
	{
		AmpApiCallbacks ampApi = AmpApiFactory.newInstance( baseUrl, AmpApiCallbacks.class );
		Call<LoginResponse> loginResponseCall = ampApi.authenticateConventional( username, password );
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
