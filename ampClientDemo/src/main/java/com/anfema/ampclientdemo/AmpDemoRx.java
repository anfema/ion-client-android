package com.anfema.ampclientdemo;

import android.content.Context;

import com.anfema.ampclient.AmpClient;
import com.anfema.ampclient.utils.Log;
import com.anfema.ampclient.utils.RxUtils;

public class AmpDemoRx
{
	private Context appContext;

	public AmpDemoRx( Context appContext )
	{
		this.appContext = appContext;
	}

	public void execute()
	{
		//		RxDebugHooks.enableObservableHook();

		//		getCollection();
		getAllPages();
	}

	private void getCollection()
	{
		Amp.client( appContext )
				.doOnNext( ampClient -> Log.d( "***** Demo ******", ": got initialized AMP client: " + ampClient ) )
				.flatMap( AmpClient::getCollection )
				.doOnNext( collection -> Log.d( "***** Demo ******", ": received collection" ) )
				.subscribe( collection -> Log.d( collection.toString() ), RxUtils.DEFAULT_EXCEPTION_HANDLER, () -> Log.d( "Collection downloaded" ) );
	}

	private void getAllPages()
	{
		Amp.client( appContext )
				.flatMap( AmpClient::getAllPages )
				.subscribe( page -> Log.d( page.toString() ), RxUtils.DEFAULT_EXCEPTION_HANDLER, () -> Log.d( "All pages downloaded" ) );
	}

	private void getPage( String pageIdentifier )
	{
		Amp.client( appContext )
				.flatMap( ampClient -> ampClient.getPage( pageIdentifier ) )
				.subscribe( page -> Log.d( page.toString() ), RxUtils.DEFAULT_EXCEPTION_HANDLER, () -> Log.d( "Page downloaded" ) );
	}
}