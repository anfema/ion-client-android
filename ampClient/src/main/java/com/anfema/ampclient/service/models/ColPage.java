package com.anfema.ampclient.service.models;

import org.joda.time.DateTime;

public class ColPage
{
	public String identifier;

	public String parent;

	public DateTime last_changed;

	public String title;

	public String thumbnail;

	public String layout;

	@Override
	public String toString()
	{
		return "ColPage [identifier = " + identifier + ", parent = " + parent + ", last_changed = " + last_changed + ", title = " + title + ", thumbnail = " + thumbnail + ", layout = " + layout + "]";
	}
}
