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
	 * Content is available for one or multiple locales. There is an extra copy of the contents for each translation.
	 */
	public ArrayList<Translation> translations;

	/**
	 * page identifier of the parent's page
	 */
	public String parent;

	/**
	 * page identifiers of sub-pages
	 */
	public ArrayList<String> children;

	/**
	 * Retrieve a translation by locale
	 *
	 * @param locale e.g. "de_DE"
	 * @return null, if there are no translations<br>
	 * translation matching the locale if found<br>
	 * the first element of translations if no matching local was found
	 */
	public Translation getTranslation( String locale )
	{
		if ( translations == null || translations.size() == 0 )
		{
			return null;
		}

		if ( locale != null )
		{
			for ( Translation translation : translations )
			{
				if ( locale.equals( translation.locale ) )
				{
					return translation;
				}
			}
		}
		// if no translation was found for locale, then return first/"default" element
		return translations.get( 0 );
	}

	/**
	 * Convenience method to replace {@link Page#getTranslation(String)} appended by {@link Translation#getContent()}.
	 */
	public AContent getContent( String locale )
	{
		Translation translation = getTranslation( locale );
		if ( translation == null )
		{
			return null;
		}
		return translation.getContent();
	}

	@Override
	public String toString()
	{
		return "Page [identifier = " + identifier + ", collection = " + collection + ", last_changed = " + last_changed + ", translations = " + translations + ", parent = " + parent + ", children = " + children + "]";
	}
}
