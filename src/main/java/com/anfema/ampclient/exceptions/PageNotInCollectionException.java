package com.anfema.ampclient.exceptions;

import android.content.res.Resources.NotFoundException;

public class PageNotInCollectionException extends NotFoundException
{
	public PageNotInCollectionException( String collectionIdentifier, String pageIdentifier )
	{
		super( "In collection " + collectionIdentifier + " is no page entry with identifier " + pageIdentifier );
	}
}
