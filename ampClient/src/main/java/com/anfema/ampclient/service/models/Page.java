package com.anfema.ampclient.service.models;

import org.joda.time.DateTime;

public class Page
{
	public String identifier;

	public String collection;

	public DateTime last_changed;

	public Translation[] translations;

	public String parent;

	public String[] children;

	@Override
	public String toString()
	{
		return "Page [identifier = " + identifier + ", collection = " + collection + ", last_changed = " + last_changed + ", translations = " + translations + ", parent = " + parent + ", children = " + children + "]";
	}
}
