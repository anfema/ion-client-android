package com.anfema.ionclient.interceptors;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AdditionalHeadersInterceptor implements Interceptor
{
	@NonNull
	private final Map<String, String> additionalHeaders;

	public AdditionalHeadersInterceptor( @NonNull Map<String, String> additionalHeaders )
	{
		this.additionalHeaders = additionalHeaders;
	}

	@Override
	public Response intercept( Chain chain ) throws IOException
	{
		Request.Builder request = chain.request().newBuilder();
		for ( Entry<String, String> header : additionalHeaders.entrySet() )
		{
			request.addHeader( header.getKey(), header.getValue() );
		}
		return chain.proceed( request.build() );
	}

}
