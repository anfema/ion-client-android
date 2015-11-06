package com.anfema.ampclient;

import com.anfema.ampclient.service.AmpApi;
import com.anfema.ampclient.service.AmpApiFactory;
import com.anfema.ampclient.service.models.CollectionsResponse;
import com.anfema.ampclient.service.models.PagesResponse;

import retrofit.Call;

public class AmpClient implements AmpClientAPI
{
	private AmpApi ampApi;
	private String collectionIdentifier;

	/**
	 * mandatory to initialize the client
	 */
	public void init( String baseUrl, String collectionIdentifier )
	{
		ampApi = AmpApiFactory.newInstance( baseUrl );
		this.collectionIdentifier = collectionIdentifier;
	}


	@Override
	public Call<String> authenticate( String username, String password )
	{
		// TODO
		ampApi.authenticate( username, password );
		return null;
	}

	@Override
	public Call<CollectionsResponse> getCollection()
	{
		// TODO
		return null;
	}

	@Override
	public Call<PagesResponse> getPage( String pageIdentifier )
	{
		// TODO
		return null;
	}

	/// Singleton
	private static AmpClient instance = new AmpClient();

	public static AmpClient getInstance()
	{
		return instance;
	}
	/// Singleton END
}
