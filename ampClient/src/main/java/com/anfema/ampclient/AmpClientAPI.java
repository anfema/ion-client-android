package com.anfema.ampclient;

import com.anfema.ampclient.service.models.CollectionResponse;
import com.anfema.ampclient.service.models.LoginResponse;
import com.anfema.ampclient.service.models.PageResponse;

import retrofit.Call;
import rx.Observable;

public interface AmpClientAPI
{
	Call<CollectionResponse> getCollection();

	Call<PageResponse> getPage( String pageIdentifier );

	Observable<PageResponse> getPageRx( String pageIdentifier );

	Observable<CollectionResponse> getCollectionRx();
}
