package com.anfema.ampclient.interceptors;

import android.content.Context;

import com.anfema.ampclient.utils.Installation;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class DeviceIdHeaderInterceptor implements Interceptor
{
	private final Context context;

	public DeviceIdHeaderInterceptor( Context context )
	{
		this.context = context;
	}

	@Override
	public Response intercept( Chain chain ) throws IOException
	{
		return requestWithDeviceIdHeader( context, chain );
	}

	public static Response requestWithDeviceIdHeader( Context context, Chain chain ) throws IOException
	{
		Request newRequest = chain.request().newBuilder()
				.addHeader( "X-DEVICEID", Installation.id( context ) )
				.build();
		return chain.proceed( newRequest );
	}
}
