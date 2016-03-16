package com.anfema.ionclient.utils;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;

public class PendingDownloadHandler<K, T>
{
	private volatile Map<K, Observable<T>> runningDownloads = new HashMap<>();

	public Observable<T> starting( K key, Observable<T> newObservable )
	{
		Observable<T> runningDownload = runningDownloads.get( key );
		if ( runningDownload == null )
		{
			runningDownload = newObservable.share();
			runningDownloads.put( key, runningDownload );
		}
		return runningDownload;
	}

	public void finished( K key )
	{
		runningDownloads.remove( key );
	}
}
