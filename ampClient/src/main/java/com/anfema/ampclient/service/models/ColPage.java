package com.anfema.ampclient.service.models;

public class ColPage
{
	private String identifier;

	private String parent;

	private String last_changed;

	private String title;

	private String thumbnail;

	private String layout;

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier( String identifier )
	{
		this.identifier = identifier;
	}

	public String getParent()
	{
		return parent;
	}

	public void setParent( String parent )
	{
		this.parent = parent;
	}

	public String getLastChanged()
	{
		return last_changed;
	}

	public void setLastChanged( String last_changed )
	{
		this.last_changed = last_changed;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public String getThumbnail()
	{
		return thumbnail;
	}

	public void setThumbnail( String thumbnail )
	{
		this.thumbnail = thumbnail;
	}

	public String getLayout()
	{
		return layout;
	}

	public void setLayout( String layout )
	{
		this.layout = layout;
	}

	@Override
	public String toString()
	{
		return "ColPage [identifier = " + identifier + ", parent = " + parent + ", last_changed = " + last_changed + ", title = " + title + ", thumbnail = " + thumbnail + ", layout = " + layout + "]";
	}
}
