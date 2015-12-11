package com.anfema.ampclient.utils;

import android.content.Context;

import java.io.File;

public class DatabaseUtils
{
	public static File getPath( String collectionIdentifier, Context context )
	{
		return context.getDatabasePath( getName( collectionIdentifier ) );
	}

	public static String getName( String collectionIdentifier )
	{
		return "fts_" + collectionIdentifier;
	}
}
