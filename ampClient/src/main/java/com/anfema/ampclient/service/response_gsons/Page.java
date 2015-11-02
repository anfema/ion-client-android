package com.anfema.ampclient.service.response_gsons;

public class Page
{
	private String identifier;

	private String last_changed;

	private String parent;

	private String[] children;

	private Translation[] translations;

	private String collection;

	public Translation[] getTranslations()
	{
		return translations;
	}

	public void setTranslations( Translation[] translations )
	{
		this.translations = translations;
	}

	public String[] getChildren()
	{
		return children;
	}

	public void setChildren( String[] children )
	{
		this.children = children;
	}

	public String getParent()
	{
		return parent;
	}

	public void setParent( String parent )
	{
		this.parent = parent;
	}

	public String getLast_changed()
	{
		return last_changed;
	}

	public void setLast_changed( String last_changed )
	{
		this.last_changed = last_changed;
	}

	public String getCollection()
	{
		return collection;
	}

	public void setCollection( String collection )
	{
		this.collection = collection;
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
		return "ClassPojo [translations = " + translations + ", children = " + children + ", parent = " + parent + ", last_changed = " + last_changed + ", collection = " + collection + ", identifier = " + identifier + "]";
	}
}
