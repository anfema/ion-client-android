package com.anfema.ionclient.utils;

import android.support.annotation.NonNull;

import com.anfema.ionclient.BuildConfig;

public class FileProviderUtils
{
	/**
	 * must be consist with file provider authority of Manifest
	 */
	@NonNull
	public static String getAuthority()
	{
		return BuildConfig.APPLICATION_ID + ".file_provider";
	}
}
