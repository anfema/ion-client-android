package com.anfema.ampclient;

import com.anfema.ampclient.service.AmpApi;
import com.anfema.ampclient.service.AmpApiFactory;
import com.anfema.ampclient.models.responses.LoginResponse;
import com.anfema.ampclient.utils.Log;
import com.anfema.ampclient.utils.RxUtils;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import rx.Observable;

/**
 * Use AMPs internal login request and retrieve API token.
 */
public class AmpAuthenticator
{
	public interface TokenCallback
	{
		void onToken( String apiToken, int responseCode );
	}

	public static Observable<String> requestApiToken( String baseUrl, String username, String password )
	{
		AmpApi ampApi = AmpApiFactory.newInstance( baseUrl );
		return ampApi.authenticate( username, password )
				.map( LoginResponse::getToken )
				.doOnError( RxUtils.DEFAULT_EXCEPTION_HANDLER )
				.compose( RxUtils.applySchedulers() );
	}

	public static void requestApiTokenConventional( String baseUrl, String username, String password, TokenCallback tokenReceiver )
	{
		AmpApi ampApi = AmpApiFactory.newInstance( baseUrl );
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
