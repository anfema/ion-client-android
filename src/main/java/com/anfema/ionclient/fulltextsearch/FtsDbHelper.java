package com.anfema.ionclient.fulltextsearch;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Provides access to downloaded full text search databases
 */
class FtsDbHelper extends SQLiteOpenHelper
{
	public static final int DATABASE_VERSION = 1;

	FtsDbHelper( String collectionIdentifier, Context context )
	{
		super( context, FtsDbUtils.getName( collectionIdentifier ), null, DATABASE_VERSION );
	}

	@Override
	public void onCreate( SQLiteDatabase db )
	{
		// do nothing
	}

	@Override
	public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
	{
		// do nothing
	}
}
