package com.anfema.ionclient.caching.index;

import android.content.Context;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.utils.HashUtils;
import com.anfema.ionclient.utils.Log;

import org.joda.time.DateTime;

import java.io.File;

import okhttp3.HttpUrl;


public class FileCacheIndex extends CacheIndex
{
	/**
	 * checksum of file version in local storage
	 */
	private String checksum;

	/**
	 * Needs to be set to the time, the file is saved
	 */
	private DateTime lastUpdated;

	public FileCacheIndex( String filename, String checksum, DateTime lastUpdated )
	{
		super( filename );
		this.checksum = checksum;
		this.lastUpdated = lastUpdated;
	}

	/**
	 * Use MD5 of request URL as filename
	 */
	public FileCacheIndex( HttpUrl requestUrl, String checksum, DateTime lastUpdated )
	{
		super( requestUrl );
		this.checksum = checksum;
		this.lastUpdated = lastUpdated;
	}

	public String getChecksum()
	{
		return checksum;
	}

	public void setChecksum( String checksum )
	{
		this.checksum = checksum;
	}

	public DateTime getLastUpdated()
	{
		return lastUpdated;
	}

	public void setLastUpdated( DateTime lastUpdated )
	{
		this.lastUpdated = lastUpdated;
	}

	public boolean isOutdated( String serverChecksum )
	{
		return !checksum.equals( serverChecksum );
	}

	// save & retrieve

	public static FileCacheIndex retrieve( String requestUrl, IonConfig config, Context context )
	{
		return CacheIndexStore.retrieve( requestUrl, FileCacheIndex.class, config, context );
	}

	/**
	 * @param checksum can be null
	 */
	public static void save( String requestUrl, File file, IonConfig config, String checksum, Context context )
	{
		if ( file == null )
		{
			Log.e( "File Cache Index", "Could not save cache index for " + requestUrl + "\nBecause file is null." );
			return;
		}

		if ( checksum == null )
		{
			checksum = "sha256:" + HashUtils.getSha256( file );
		}
		FileCacheIndex cacheIndex = new FileCacheIndex( requestUrl, checksum, DateTime.now() );
		CacheIndexStore.save( requestUrl, cacheIndex, config, context );
	}
}
