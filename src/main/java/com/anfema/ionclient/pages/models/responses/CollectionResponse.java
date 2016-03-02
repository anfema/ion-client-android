package com.anfema.ionclient.pages.models.responses;


import com.anfema.ionclient.pages.models.Collection;

public class CollectionResponse
{
	public CollectionResponse()
	{
	}

	public CollectionResponse( Collection collection )
	{
		this.collection = new Collection[]{ collection };
	}

	private Collection[] collection;

	public Collection getCollection()
	{
		return collection[ 0 ];
	}

	@Override
	public String toString()
	{
		return "CollectionsResponse [collection = " + getCollection() + "]";
	}
}
