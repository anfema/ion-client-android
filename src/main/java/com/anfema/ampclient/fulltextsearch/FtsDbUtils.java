package com.anfema.ampclient.fulltextsearch;

import android.content.Context;

import java.io.File;

public class FtsDbUtils
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
