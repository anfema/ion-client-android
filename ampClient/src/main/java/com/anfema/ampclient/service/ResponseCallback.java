package com.anfema.ampclient.service;

import retrofit.Callback;

public abstract class ResponseCallback implements Callback
{
	@Override
	public void onFailure( Throwable t )
	{
		//TODO handle network and unexpected errors
	}
}
