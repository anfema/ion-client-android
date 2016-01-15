package com.anfema.ampclient.pages.models;

import com.anfema.ampclient.pages.models.contents.AContent;

import org.joda.time.DateTime;

import java.util.ArrayList;

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

	/**
	 * e.g. "de_DE"
	 */
	public String locale;

	/**
	 * page identifier of the parent's page
	 */
	public String parent;

	/**
	 * Only one item, which is of type "containercontent", holding all contents of page in nested form.
	 */
	private ArrayList<AContent> content;

	/**
	 * page identifiers of sub-pages
	 */
	public ArrayList<String> children;

	/**
	 * Access content, i.e. containercontent
	 *
	 * @return first item of array if exists, or null otherwise
	 */
	public AContent getContent()
	{
		if ( content == null || content.size() == 0 )
		{
			return null;
		}
		return content.get( 0 );
	}

	@Override
	public String toString()
	{
		String contentString = content == null || content.size() == 0 ? "no content" : content.get( 0 ).toString();
		return "Page [identifier = " + identifier + ", collection = " + collection + ", last_changed = " + last_changed + ", content = " + contentString + ", parent = " + parent + ", children = " + children + "]";
	}
}
