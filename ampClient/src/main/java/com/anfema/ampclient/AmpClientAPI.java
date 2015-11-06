package com.anfema.ampclient;

import com.anfema.ampclient.service.models.CollectionsResponse;
import com.anfema.ampclient.service.models.PagesResponse;

import retrofit.Call;

public interface AmpClientAPI
{
	Call<String> authenticate( String username, String password );

	Call<CollectionsResponse> getCollection();

	Call<PagesResponse> getPage( String pageIdentifier );
}
