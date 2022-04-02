package com.anfema.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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
}
