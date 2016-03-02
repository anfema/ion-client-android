package com.anfema.ampclient.utils;

import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import rx.Observable;

public class RunningDownloadHandler<T>
{
	private volatile Map<HttpUrl, Observable<T>> runningDownloads = new HashMap<>();

	public Observable<T> starting( HttpUrl url, Observable<T> newObservable )
	{
		Observable<T> runningDownload = runningDownloads.get( url );
		if ( runningDownload == null )
		{
			runningDownload = newObservable.share();
			runningDownloads.put( url, runningDownload );
		}
		return runningDownload;
	}

	public void finished( HttpUrl url )
	{
		runningDownloads.remove( url );
	}
}
