package com.anfema.ampclient.models.responses;


import com.anfema.ampclient.models.Collection;

public class CollectionResponse
{
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
