package com.anfema.ampclient.pages.models;

import android.support.annotation.NonNull;

import com.anfema.ampclient.exceptions.PageNotInCollectionException;

import org.joda.time.DateTime;

import java.util.ArrayList;

import rx.Observable;

public class Collection
{
	/**
	 * AMP internal id of the collection
	 */
	public String id;

	/**
	 * Usually, there is one collection that is used per app. Thus, the collection identifier can be hard-coded and passed to AMP client configuration.
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

	/**
	 * Find out latest change date of all pages. If no pages exist a very early date is returned.
	 */
	@NonNull
	public DateTime getLastChanged()
	{
		DateTime lastChanged = new DateTime( 1970, 1, 1, 0, 0 );

		for ( PagePreview pagePreview : pages )
		{
			if ( lastChanged.isBefore( pagePreview.last_changed ) )
			{
				lastChanged = pagePreview.last_changed;
			}
		}
		return lastChanged;
	}

	@Override
	public String toString()
	{
		return "Collection [id = " + id + ", identifier = " + identifier + ", name = " + name + ", default_locale = " + default_locale
				+ ", fts_db " + fts_db + ", archive " + archive + ", pages = " + pages + "]";
	}
}
