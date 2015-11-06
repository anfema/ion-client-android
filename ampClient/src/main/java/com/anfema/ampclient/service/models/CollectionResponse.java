package com.anfema.ampclient.service.models;


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
