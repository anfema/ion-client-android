package com.anfema.ampclient;

import android.content.Context;

import com.anfema.ampclient.utils.ContextUtils;
import com.anfema.ampclient.utils.RxUtils;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;

public class TokenHolder
{
	/// Multiton

	private static Map<Class<? extends AmpClientConfig>, String> tokens = new HashMap<>();

	public static Observable<String> getToken( Class<? extends AmpClientConfig> configClass, Context context )
	{
		context = ContextUtils.getApplicationContext( context );

		if ( tokens == null )
		{
			tokens = new HashMap<>();
		}

		String storedToken = tokens.get( configClass );
		if ( storedToken != null )
		{
			return Observable.just( storedToken );
		}

		try
		{
			AmpClientConfig ampClientConfig = configClass.newInstance();
			return ampClientConfig.requestApiToken( context )
					.doOnNext( token -> tokens.put( configClass, token ) )
					.compose( RxUtils.applySchedulers() );
		}
		catch ( InstantiationException e )
		{
			return Observable.error( e );
		}
		catch ( IllegalAccessException e )
		{
			return Observable.error( e );
		}
	}
}
