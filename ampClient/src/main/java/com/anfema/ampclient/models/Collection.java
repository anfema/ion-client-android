package com.anfema.ampclient.models;

import java.util.ArrayList;

public class Collection
{
	public String id;

	public String identifier;

	public String name;

	public String default_locale;

	public ArrayList<ColPage> pages;

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier( String identifier )
	{
		this.identifier = identifier;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String getDefault_locale()
	{
		return default_locale;
	}

	public void setDefault_locale( String default_locale )
	{
		this.default_locale = default_locale;
	}

	public ArrayList<ColPage> getPages()
	{
		return pages;
	}

	public void setPages( ArrayList<ColPage> pages )
	{
		this.pages = pages;
	}

	@Override
	public String toString()
	{
		return "Collection [id = " + id + ", pages = " + pages + ", default_locale = " + default_locale + ", name = " + name + ", identifier = " + identifier + "]";
	}
}
