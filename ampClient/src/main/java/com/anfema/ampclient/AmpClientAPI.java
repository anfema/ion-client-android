package com.anfema.ampclient;

import com.anfema.ampclient.service.models.Collection;
import com.anfema.ampclient.service.models.CollectionResponse;
import com.anfema.ampclient.service.models.LoginResponse;
import com.anfema.ampclient.service.models.Page;
import com.anfema.ampclient.service.models.PageResponse;

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
