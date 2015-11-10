package com.anfema.ampclientdemo;

import android.app.Application;

import com.facebook.stetho.Stetho;

public class DemoApplication extends Application
{
	public void onCreate()
	{
		super.onCreate();
		Stetho.initializeWithDefaults( this );
	}
}
