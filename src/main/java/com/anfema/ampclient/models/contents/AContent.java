package com.anfema.ampclient.models.contents;

public abstract class AContent
{
	public String type;

	public String variation;

	public String outlet;

	public boolean is_searchable; // not used by container outlet

	@Override
	public String toString()
	{
		return "Content [type = " + type + ", variation = " + variation + ", outlet = " + outlet + ", type = " + type + ", outlet = " + outlet
				+ ", is_searchable = " + is_searchable + ", variation = " + variation + "]";
	}
}