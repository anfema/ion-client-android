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
	 * use collection identifier specified in {@link AmpClient#config}
	 */
	Observable<Collection> getCollection();

	/**
	 * use collection identifier specified in {@link AmpClient#config}
	 */
	Observable<Page> getPage( String pageIdentifier );

	/**
	 * use collection identifier specified in {@link AmpClient#config}
	 */
	Observable<Page> getAllPages();
}
