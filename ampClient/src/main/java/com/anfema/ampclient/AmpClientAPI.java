package com.anfema.ampclient;

import com.anfema.ampclient.models.Collection;
import com.anfema.ampclient.models.responses.CollectionResponse;
import com.anfema.ampclient.models.Page;
import com.anfema.ampclient.models.responses.PageResponse;

import retrofit.Call;
import rx.Observable;

public interface AmpClientAPI
{
	Call<CollectionResponse> getCollectionConventional();

	Call<PageResponse> getPageConventional( String pageIdentifier );

	Observable<Page> getPage( String pageIdentifier );

	Observable<Collection> getCollection();

	Observable<Page> getAllPages();
}
