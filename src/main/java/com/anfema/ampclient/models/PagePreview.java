package com.anfema.ampclient.models;

import org.joda.time.DateTime;

import java.util.Map;

/**
 * Meta/preview information for pages. It does not hold the content for a page.
 */
public class PagePreview
{
	/**
	 * page identifier
	 */
	public String identifier;

	/**
	 * page identifier of the parent's page
	 */
	public String parent;

	/**
	 * date when published page has been updated the last time
	 */
	public DateTime last_changed;

	/**
	 * Meta contains additional, flexible data.
	 * Since it is very application-specific the provided data is stored as key-value-pairs, simply consisting of strings.
	 */
	public Map<String, String> meta;


	/**
	 * Convenience method to compare values from meta with other ones.
	 *
	 * @param metaKey      The key of meta value (e.g. "title" or "thumbnail")
	 * @param compareValue A value to compare the concerning meta value with.
	 * @return true, if meta value exists and equals {@param compareValue}
	 */
	public boolean metaEquals( String metaKey, String compareValue )
	{
		return meta != null && meta.containsKey( metaKey ) && meta.get( metaKey ).equals( compareValue );
	}

	/**
	 * the page's layout
	 */
	public String layout;

	@Override
	public String toString()
	{
		return "PagePreview [identifier = " + identifier + ", parent = " + parent + ", last_changed = " + last_changed + ", layout = " + layout + "]";
	}
}
