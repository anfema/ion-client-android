package com.anfema.ampclient;

import com.anfema.ampclient.models.Collection;
import com.anfema.ampclient.models.Page;

import rx.Observable;

public interface AmpClientApi
{
	Observable<Collection> getCollection( String collectionIdentifier );

	Observable<Page> getPage( String collectionIdentifier, String pageIdentifier );

	Observable<Page> getAllPages( String collectionIdentifier );

	/**
	 * assume there is one collection identifier, specified in {@link AmpClientConfigMethods#getCollectionIdentifier}
	 */
	Observable<Collection> getCollection();

	/**
	 * assume there is one collection identifier, specified in {@link AmpClientConfigMethods#getCollectionIdentifier}
	 */
	Observable<Page> getPage( String pageIdentifier );

	/**
	 * assume there is one collection identifier, specified in {@link AmpClientConfigMethods#getCollectionIdentifier}
	 */
	Observable<Page> getAllPages();
}
