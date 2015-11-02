package com.anfema.ampclient.service.response_gsons;

public class Page
{
	private String identifier;

	private String collection;

	private String last_changed;

	private Translation[] translations;

	private String parent;

	private String[] children;

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier( String identifier )
	{
		this.identifier = identifier;
	}

	public String getCollection()
	{
		return collection;
	}

	public void setCollection( String collection )
	{
		this.collection = collection;
	}

	public String getLast_changed()
	{
		return last_changed;
	}

	public void setLast_changed( String last_changed )
	{
		this.last_changed = last_changed;
	}

	public Translation[] getTranslations()
	{
		return translations;
	}

	public void setTranslations( Translation[] translations )
	{
		this.translations = translations;
	}

	public String getParent()
	{
		return parent;
	}

	public void setParent( String parent )
	{
		this.parent = parent;
	}

	public String[] getChildren()
	{
		return children;
	}

	public void setChildren( String[] children )
	{
		this.children = children;
	}

	@Override
	public String toString()
	{
		return "Page [identifier = " + identifier + ", collection = " + collection + ", last_changed = " + last_changed + ", translations = " + translations + ", parent = " + parent + ", children = " + children + "]";
	}
}
