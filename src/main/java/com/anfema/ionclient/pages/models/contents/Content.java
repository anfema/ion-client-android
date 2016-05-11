package com.anfema.ionclient.pages.models.contents;

import android.support.annotation.NonNull;

public class Content implements Comparable<Content>
{
	public String variation;

	/**
	 * identifies the content, specifies where it is supposed to be used in a layout
	 */
	public String outlet;

	/**
	 * When there are more contents of same kind an array, position indicates their order.
	 */
	public long position;

	public boolean is_searchable; // not used by container outlet

	@Override
	public String toString()
	{
		return "Content [class: " + getClass().getSimpleName() + ", outlet = " + outlet + ", variation = " + variation + ", position = " + position
				+ ", is_searchable = " + is_searchable + "]";
	}

	/**
	 * Sort by positions ascending
	 */
	@Override
	public int compareTo( @NonNull Content another )
	{
		// Alternatively, one long could be subtracted from the other resulting in a long in the right range. However, casting to int might not be safe.
		if ( position == another.position )
		{
			return 0;
		}
		return position < another.position ? -1 : 1;
	}
}