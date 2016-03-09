package com.anfema.ionclient.pages.models.contents;

public class Content
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
}