package com.anfema.ionclient.pages.models;

import com.anfema.ionclient.pages.models.contents.Content;

import org.joda.time.DateTime;

import java.util.List;

/**
 * A page holds all the information about its content.
 */
public class Page
{
	/**
	 * Page identifier – matches the page_identifier header of the pages requests.
	 */
	public String identifier;

	/**
	 * identifier of the collection the page belongs to
	 */
	public String collection;

	/**
	 * date when published page has been updated the last time
	 */
	public DateTime last_changed;

	public String layout;

	/**
	 * e.g. "de_DE"
	 */
	public String locale;

	/**
	 * page identifier of the parent's page
	 */
	public String parent;

	/**
	 * page identifiers of sub-pages
	 */
	public List<String> children;

	public List<Content> contents;

	@Override
	public String toString()
	{
		return "Page [identifier = " + identifier + ", collection = " + collection + ", last_changed = " + last_changed + ", parent = " + parent + ", children = " + children + "]";
	}
}
