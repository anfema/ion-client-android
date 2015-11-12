package com.anfema.ampclient.utils;

import rx.plugins.DebugHook;
import rx.plugins.DebugNotification;
import rx.plugins.DebugNotificationListener;
import rx.plugins.RxJavaPlugins;

public class RxDebugHooks
{
	public static final String RX_DEBUG = "RX-DEBUG";

	public static DebugHook debugHook = new DebugHook( new DebugNotificationListener()
	{
		public Object onNext( DebugNotification n )
		{
			Log.v( RX_DEBUG, "onNext on " + n );
			return super.onNext( n );
		}


		public Object start( DebugNotification n )
		{
			Log.v( RX_DEBUG, "start on " + n );
			return super.start( n );
		}


		public void complete( Object context )
		{
			Log.v( RX_DEBUG, "complete on " + context );
		}

		public void error( Object context, Throwable e )
		{
			Log.e( RX_DEBUG, "error on " + context );
			Log.ex( RX_DEBUG, e );
		}
	} );

	public static void enableObservableHook()
	{
		RxJavaPlugins.getInstance().registerObservableExecutionHook( debugHook );
	}
}
