package com.anfema.ionclient.pages.models;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;

/**
 * Meta/preview information for pages. It does not hold the full content of a {@link Page}.
 */
@SuppressWarnings("unused")
public class PagePreview implements Comparable<PagePreview>
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
	 * Position indicates the order among the pages.
	 */
	private long position;

	/**
	 * the page's layout
	 */
	public String layout;

	/**
	 * Meta contains additional, flexible data.
	 * Since it is very application-specific the provided data is stored as key-value-pairs, simply consisting of strings.
	 */
	@NonNull
	public Meta meta = new Meta();

	/**
	 * Sort by positions ascending
	 */
	@Override
	public int compareTo( @NonNull PagePreview another )
	{
		// Alternatively, one long could be subtracted from the other resulting in a long in the right range. However, casting to int might not be safe.
		if ( position == another.position )
		{
			return 0;
		}
		return position < another.position ? -1 : 1;
	}

	@Override
	public String toString()
	{
		return "PagePreview [identifier = " + identifier + ", parent = " + parent + ", last_changed = " + last_changed + ", position = " + position + ", layout = " + layout + "]";
	}
}
