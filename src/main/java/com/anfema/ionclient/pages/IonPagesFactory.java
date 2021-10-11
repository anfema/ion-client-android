package com.anfema.ionclient.pages;

import android.content.Context;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.interceptors.AdditionalHeadersInterceptor;
import com.anfema.ionclient.interceptors.WriteIonCacheInterceptor;
import com.anfema.ionclient.interceptors.DeviceIdHeaderInterceptor;

import java.util.ArrayList;

import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

import static com.anfema.ionclient.utils.IonLog.INFO;
import static com.anfema.ionclient.utils.IonLog.VERBOSE;

public class IonPagesFactory
{
	public static IonPages newInstance( IonConfig config, Context context )
	{
		ArrayList<Interceptor> interceptors = new ArrayList<>();
		interceptors.add( new DeviceIdHeaderInterceptor( context ) );
		if ( IonConfig.logLevel <= INFO && IonConfig.logLevel >= VERBOSE )
		{
			interceptors.add( new HttpLoggingInterceptor().setLevel( Level.BODY ) );
		}
		interceptors.add( new WriteIonCacheInterceptor( config, context ) );
		interceptors.add( new AdditionalHeadersInterceptor( config.additionalHeaders ) );

		return new IonPagesWithCaching( config, context, interceptors );
	}
}