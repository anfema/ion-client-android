package com.anfema.ampclientdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.anfema.ampclient.utils.Log;

public class MainActivity extends AppCompatActivity
{
	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		//		setContentView( R.layout.activity_main );

		new AmpTests( this ).execute();

		runOnUiThread( () -> {

		} );

		runOnUiThread( () -> Log.d( "Test" ) );

	}
}
