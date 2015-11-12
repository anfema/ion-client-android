package com.anfema.ampclient;

import com.anfema.ampclient.models.Collection;
import com.anfema.ampclient.models.Page;

import rx.Observable;

public interface AmpClientApi
{
	Observable<Page> getPage( String pageIdentifier );

	Observable<Collection> getCollection();

	Observable<Page> getAllPages();
}
