package com.anfema.ampclient.callbackapproach;

import com.anfema.ampclient.models.responses.CollectionResponse;
import com.anfema.ampclient.models.responses.PageResponse;

import retrofit.Call;

public interface AmpClientApiCallbacks
{
	Call<CollectionResponse> getCollectionConventional();

	Call<PageResponse> getPageConventional( String pageIdentifier );

	// for "get all pages" it is not possible to return a single call
}
