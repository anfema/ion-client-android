package com.anfema.ionclient.pages;

import android.content.Context;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.interceptors.CachingInterceptor;
import com.anfema.ionclient.interceptors.DeviceIdHeaderInterceptor;
import com.anfema.ionclient.interceptors.RequestLogger;

import java.util.ArrayList;

import okhttp3.Interceptor;

public class IonPagesFactory
{
	public static IonPages newInstance( IonConfig config, Context context )
	{
		ArrayList<Interceptor> interceptors = new ArrayList<>();
		interceptors.add( new DeviceIdHeaderInterceptor( context ) );
		interceptors.add( new RequestLogger( "Network Request" ) );
		interceptors.add( new CachingInterceptor( config, context ) );

		return new IonPagesWithCaching( config, context, interceptors );
	}
}
