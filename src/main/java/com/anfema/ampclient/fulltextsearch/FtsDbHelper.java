package com.anfema.ampclient.fulltextsearch;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.anfema.ampclient.utils.DatabaseUtils;

/**
 * Provides access to downloaded full text search databases
 */
public class FtsDbHelper extends SQLiteOpenHelper
{
	public static final int DATABASE_VERSION = 1;

	public FtsDbHelper( String collectionIdentifier, Context context )
	{
		super( context, DatabaseUtils.getName( collectionIdentifier ), null, DATABASE_VERSION );
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
