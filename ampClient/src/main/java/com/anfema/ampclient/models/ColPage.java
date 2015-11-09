package com.anfema.ampclient.models;

import org.joda.time.DateTime;

/**
 * Meta/preview information for pages. It does not hold the content for a page.
 */
public class ColPage
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
	 * page's title can be displayed in a preview
	 */
	public String title;

	/**
	 * image url for thumbnail to display in a preview
	 */
	public String thumbnail;

	/**
	 * the page's layout
	 */
	public String layout;

	@Override
	public String toString()
	{
		return "ColPage [identifier = " + identifier + ", parent = " + parent + ", last_changed = " + last_changed + ", title = " + title + ", thumbnail = " + thumbnail + ", layout = " + layout + "]";
	}
}
