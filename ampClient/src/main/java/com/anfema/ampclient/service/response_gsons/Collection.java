package com.anfema.ampclient.service.response_gsons;

import java.util.ArrayList;

public class Collection
{
	private String id;

	private ArrayList<Page> pages;

	private String default_locale;

	private String name;

	private String identifier;

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public ArrayList<Page> getPages()
	{
		return pages;
	}

	public void setPages( ArrayList<Page> pages )
	{
		this.pages = pages;
	}

	public String getDefault_locale()
	{
		return default_locale;
	}

	public void setDefault_locale( String default_locale )
	{
		this.default_locale = default_locale;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier( String identifier )
	{
		this.identifier = identifier;
	}

	@Override
	public String toString()
	{
		return "Collection [id = " + id + ", pages = " + pages + ", default_locale = " + default_locale + ", name = " + name + ", identifier = " + identifier + "]";
	}
}
