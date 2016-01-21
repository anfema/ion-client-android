package com.anfema.ampclient.pages;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.interceptors.CachingInterceptor;
import com.anfema.ampclient.interceptors.DeviceIdHeaderInterceptor;
import com.anfema.ampclient.interceptors.RequestLogger;

import java.util.ArrayList;

import okhttp3.Interceptor;

public class AmpPagesFactory
{
	public static AmpPages newInstance( AmpConfig config, Context context )
	{
		ArrayList<Interceptor> interceptors = new ArrayList<>();
		interceptors.add( new DeviceIdHeaderInterceptor( context ) );
		interceptors.add( new RequestLogger( "Network Request" ) );
		interceptors.add( new CachingInterceptor( context ) );

		return new AmpPagesWithCaching( config, context, interceptors );
	}
}
