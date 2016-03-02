package com.anfema.ionclient.exceptions;

public class PageNotInCollectionException extends Exception
{
	public PageNotInCollectionException( String collectionIdentifier, String pageIdentifier )
	{
		super( "In collection " + collectionIdentifier + " is no page entry with identifier " + pageIdentifier );
	}
}
