package com.anfema.ampclient.service.models;


public class CollectionsResponse
{
	private Collection[] collection;

	public Collection getCollection()
	{
		return collection[ 0 ];
	}

	@Override
	public String toString()
	{
		return "collection = " + getCollection();
	}
}
