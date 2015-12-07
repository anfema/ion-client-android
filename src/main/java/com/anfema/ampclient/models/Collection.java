package com.anfema.ampclient.models;

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
	 * The identifier matches the collection_identifer in the API requests.
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
	public Observable<? extends DateTime> getPageLastChanged( String pageIdentifier )
	{
		for ( PagePreview pagePreview : pages )
		{
			if ( pageIdentifier.equals( pagePreview.identifier ) )
			{
				return Observable.just( pagePreview.last_changed );
			}
		}
		// return a date in the future
		return Observable.error( new PageNotInCollectionException( identifier, pageIdentifier ) );
	}

	@Override
	public String toString()
	{
		return "Collection [id = " + id + ", identifier = " + identifier + ", name = " + name + ", default_locale = " + default_locale
				+ ", fts_db " + fts_db + ", archive " + archive + ", pages = " + pages + "]";
	}
}
