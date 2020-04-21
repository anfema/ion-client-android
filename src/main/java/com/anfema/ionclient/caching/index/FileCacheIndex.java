package com.anfema.ionclient.caching.index;

import android.content.Context;
import androidx.annotation.Nullable;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.utils.DateTimeUtils;
import com.anfema.ionclient.utils.IonLog;
import com.anfema.utils.HashUtils;

import org.joda.time.DateTime;

import java.io.File;


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

	public FileCacheIndex( String checksum, DateTime lastUpdated )
	{
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
	public static void save( String requestUrl, File file, IonConfig config, String checksum, @Nullable DateTime requestTime, Context context )
	{
		if ( file == null )
		{
			IonLog.e( "File Cache Index", "Could not save cache index for " + requestUrl + "\nBecause file is null." );
			return;
		}

		if ( checksum == null )
		{
			checksum = "sha256:" + HashUtils.getSha256( file );
		}
		if ( requestTime == null )
		{
			requestTime = DateTimeUtils.now();
		}
		FileCacheIndex cacheIndex = new FileCacheIndex( checksum, requestTime );
		CacheIndexStore.save( requestUrl, cacheIndex, config, context );
	}
}
