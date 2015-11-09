package com.anfema.ampclient.models;

import java.util.ArrayList;

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
	 * page previews and meta information about them
	 */
	public ArrayList<ColPage> pages;

	@Override
	public String toString()
	{
		return "Collection [id = " + id + ", pages = " + pages + ", default_locale = " + default_locale + ", name = " + name + ", identifier = " + identifier + "]";
	}
}
