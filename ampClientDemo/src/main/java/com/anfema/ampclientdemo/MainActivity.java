package com.anfema.ampclientdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.anfema.ampclient.utils.Log;

import rx.plugins.DebugHook;
import rx.plugins.DebugNotification;
import rx.plugins.DebugNotificationListener;
import rx.plugins.RxJavaPlugins;

public class MainActivity extends AppCompatActivity
{
	public static final String RX_DEBUG = "RX-DEBUG";

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
//		setContentView( R.layout.activity_main );


		Log.d( "Test2" );

		RxJavaPlugins.getInstance().registerObservableExecutionHook( new DebugHook( new DebugNotificationListener()
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
			}
		} ) );

		new AmpTests( this ).execute();

		runOnUiThread( () -> {

		} );

		runOnUiThread( () -> Log.d( "Test" ) );

	}
}
