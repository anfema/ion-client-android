package com.anfema.ampclient.service.response_gsons;


public class CollectionsResponse
{
	private Collection[] collection;

	private Meta meta;

	public Collection[] getCollections()
	{
		return collection;
	}

	public void setCollections( Collection[] collection )
	{
		this.collection = collection;
	}

	public Meta getMeta()
	{
		return meta;
	}

	public void setMeta( Meta meta )
	{
		this.meta = meta;
	}

	@Override
	public String toString()
	{
		return "ClassPojo [collection = " + collection + ", meta = " + meta + "]";
	}
}
