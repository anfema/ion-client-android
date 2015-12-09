package com.anfema.ampclient.authorization;

import android.content.Context;

import com.anfema.ampclient.AmpClientConfigMethods;
import com.anfema.ampclient.utils.ContextUtils;
import com.anfema.ampclient.utils.RxUtils;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;

public class AuthorizationHolder
{
	private static Map<Class<? extends AmpClientConfigMethods>, String> authHeaderValues = new HashMap<>();

	public static Observable<String> getAuthHeaderValue( Class<? extends AmpClientConfigMethods> configClass, Context context )
	{
		context = ContextUtils.getApplicationContext( context );

		if ( authHeaderValues == null )
		{
			authHeaderValues = new HashMap<>();
		}

		String storedToken = authHeaderValues.get( configClass );
		if ( storedToken != null )
		{
			return Observable.just( storedToken );
		}

		try
		{
			AmpClientConfigMethods ampClientConfigMethods = configClass.newInstance();
			return ampClientConfigMethods.requestAuthorizationHeaderValue( context )
					.doOnNext( authHeaderValue -> authHeaderValues.put( configClass, authHeaderValue ) )
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
