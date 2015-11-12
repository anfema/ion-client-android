package com.anfema.ampclientdemo;

import android.content.Context;

import com.anfema.ampclient.AmpClient;

import rx.Observable;

public class Amp
{
	public static Observable<AmpClient> client( Context appContext )
	{
		return AmpClient.getInstance( DemoConfig.class, appContext );
	}
}