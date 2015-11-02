package com.anfema.ampclient.service.response_gsons;


public class CollectionsResponse
{
	private Collection[] collections;

	public Collection getCollection()
	{
		return collections[ 0 ];
	}

	@Override
	public String toString()
	{
		return "collection = " + collections[ 0 ];
	}
}
