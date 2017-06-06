package com.anfema.ionclient.pages.models;

import android.support.annotation.NonNull;

import com.anfema.ionclient.exceptions.PageNotInCollectionException;

import org.joda.time.DateTime;

import java.util.ArrayList;

import io.reactivex.Observable;

public class Collection implements SizeAware
{
	/**
	 * ION internal id of the collection
	 */
	public String id;

	/**
	 * Usually, there is one collection that is used per app. Thus, the collection identifier can be hard-coded and passed to ION client configuration.
	 * The identifier matches the collection_identifier in the API requests.
	 */
	public String identifier;

	/**
	 * displayable name of the collection
	 */
	public String name;

	/**
	 * e.g "de_DE"
	 */
	public String default_locale;


	/**
	 * Path to sqlite database, which can be used for a locally performed full text search.
	 */
	public String fts_db;

	/**
	 * Zip file containing all pages and files of collection.
	 */
	public String archive;

	/**
	 * page previews and meta information about them
	 */
	public ArrayList<PagePreview> pages;

	@NonNull
	public Observable<? extends DateTime> getPageLastChangedAsync( String pageIdentifier )
	{
		try
		{
			return Observable.just( getPageLastChanged( pageIdentifier ) );
		}
		catch ( PageNotInCollectionException e )
		{
			return Observable.error( e );
		}
	}

	@NonNull
	public DateTime getPageLastChanged( String pageIdentifier ) throws PageNotInCollectionException
	{
		for ( PagePreview pagePreview : pages )
		{
			if ( pageIdentifier.equals( pagePreview.identifier ) )
			{
				return pagePreview.last_changed;
			}
		}
		throw new PageNotInCollectionException( identifier, pageIdentifier );
	}

	public long byteCount;

	@Override
	public long byteCont()
	{
		return byteCount;
	}

	@Override
	public String toString()
	{
		return "Collection [id = " + id + ", identifier = " + identifier + ", name = " + name + ", default_locale = " + default_locale
				+ ", fts_db " + fts_db + ", archive " + archive + ", pages = " + pages + "]";
	}
}
