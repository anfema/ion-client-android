package com.anfema.ionclient.pages.models;

import com.anfema.ionclient.pages.models.contents.Content;
import com.anfema.ionclient.pages.models.contents.TextContent;

import org.joda.time.DateTime;

import java.util.ArrayList;
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

	public Content getContent( String outlet )
	{
		if ( outlet == null )
		{
			return null;
		}

		for ( Content content : contents )
		{
			if ( outlet.equals( content.outlet ) )
			{
				return content;
			}
		}
		return null;
	}

	public List<Content> getContents( String outlet )
	{
		List<Content> contents = new ArrayList<>();
		if ( outlet != null )
		{
			for ( Content content : this.contents )
			{
				if ( outlet.equals( content.outlet ) )
				{
					contents.add( content );
				}
			}
		}
		return contents;
	}

	/**
	 * Convenience method to obtain the text of a {@link com.anfema.ionclient.pages.models.contents.TextContent}.
	 */
	public String getText( String outlet )
	{
		Content content = getContent( outlet );
		if ( content != null && content instanceof TextContent )
		{
			return ( ( TextContent ) content ).text;
		}
		return null;
	}

	@Override
	public String toString()
	{
		return "Page [identifier = " + identifier + ", collection = " + collection + ", last_changed = " + last_changed + ", parent = " + parent + ", children = " + children + "]";
	}
}
