package com.anfema.ampclient.service.models;

public class ColPage
{
	public String identifier;

	public String parent;

	public String last_changed;

	public String title;

	public String thumbnail;

	public String layout;

	@Override
	public String toString()
	{
		return "ColPage [identifier = " + identifier + ", parent = " + parent + ", last_changed = " + last_changed + ", title = " + title + ", thumbnail = " + thumbnail + ", layout = " + layout + "]";
	}
}
