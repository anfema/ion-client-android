package com.anfema.ionclient.utils;


import com.anfema.ionclient.BuildConfig;

public class Config
{
	private final static FeatureState LOGCAT_LOGGING = FeatureState.DEFAULT; // Use DEFAULT for release!

	public static boolean loggingEnabled()
	{
		return checkEnabled( LOGCAT_LOGGING, BuildConfig.LOGGING_DEFAULT );
	}

	protected static boolean checkEnabled( FeatureState config, boolean buildTypeDefault )
	{
		switch ( config )
		{
		case DEFAULT:
			return buildTypeDefault;
		case ON:
			return true;
		case OFF:
			return false;
		default:
			return false;
		}
	}
}
