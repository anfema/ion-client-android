package com.anfema.ampclientdemo;

import android.content.Context;

import com.anfema.ampclient.AmpAuthenticator;
import com.anfema.ampclient.AmpClientConfig;

import rx.Observable;

public class DemoConfig extends AmpClientConfig
{
	@Override
	public String getBaseUrl( Context appContext )
	{
		return appContext.getString( R.string.base_url );
	}

	@Override
	public String getCollectionIdentifier( Context appContext )
	{
		return appContext.getString( R.string.collection_identifier );
	}

	@Override
	public Observable<String> requestApiToken( Context appContext )
	{
		return AmpAuthenticator.requestApiToken( getBaseUrl( appContext ), "admin@anfe.ma", "test" );
	}
}
