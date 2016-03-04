package com.anfema.ionclient.pages.models.contents;

public abstract class Content
{
	public String type;

	public String variation;

	public String outlet;

	/**
	 * When there are more contents in on the same level in an array, position indicates their order.
	 */
	public long position;

	public boolean is_searchable; // not used by container outlet

	@Override
	public String toString()
	{
		return "Content [type = " + type + ", variation = " + variation + ", outlet = " + outlet + ", type = " + type + ", outlet = " + outlet
				+ ", is_searchable = " + is_searchable + ", variation = " + variation + "]";
	}
}