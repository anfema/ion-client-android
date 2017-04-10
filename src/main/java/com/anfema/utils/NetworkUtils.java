package com.anfema.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class NetworkUtils
{
	public static boolean isConnected( Context context )
	{
		NetworkInfo activeNetworkInfo = getNetworkInfo( context );
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public static boolean isWifiConnected( Context context )
	{
		NetworkInfo activeNetworkInfo = getNetworkInfo( context );
		return activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.getTypeName().equalsIgnoreCase( "WIFI" );
	}

	public static NetworkInfo getNetworkInfo( Context context )
	{
		ConnectivityManager connectivityManager = ( ConnectivityManager ) context.getSystemService( Context.CONNECTIVITY_SERVICE );
		return connectivityManager.getActiveNetworkInfo();
	}

	public static void applyTimeout( OkHttpClient.Builder okHttpClientBuilder, int networkTimeout )
	{
		if ( networkTimeout > 0 )
		{
			okHttpClientBuilder.connectTimeout( networkTimeout, TimeUnit.SECONDS );
			okHttpClientBuilder.readTimeout( networkTimeout, TimeUnit.SECONDS );
			okHttpClientBuilder.writeTimeout( networkTimeout, TimeUnit.SECONDS );
		}
	}
}
