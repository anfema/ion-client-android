package com.anfema.ampclient.fulltextsearch;

import android.content.Context;

import java.io.File;

class FtsDbUtils
{
	static File getPath( String collectionIdentifier, Context context )
	{
		return context.getDatabasePath( getName( collectionIdentifier ) );
	}

	static String getName( String collectionIdentifier )
	{
		return "fts_" + collectionIdentifier;
	}
}
