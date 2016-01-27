package com.anfema.ampclient.caching;

import android.content.Context;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.utils.HashUtils;

import java.io.File;

import okhttp3.HttpUrl;


public class FileCacheIndex extends CacheIndex
{
	private String checksum;

	public FileCacheIndex( String filename, String checksum )
	{
		super( filename );
		this.checksum = checksum;
	}

	/**
	 * Use MD5 of request URL as filename
	 *
	 * @param checksum
	 */
	public FileCacheIndex( HttpUrl requestUrl, String checksum )
	{
		super( requestUrl );
		this.checksum = checksum;
	}

	public String getChecksum()
	{
		return checksum;
	}

	public void setChecksum( String checksum )
	{
		this.checksum = checksum;
	}

	public boolean isOutdated( String serverChecksum )
	{
		return !checksum.equals( serverChecksum );
	}

	// save & retrieve

	public static FileCacheIndex retrieve( String requestUrl, String collectionIdentifier, Context context )
	{
		return CacheIndexStore.retrieve( requestUrl, FileCacheIndex.class, collectionIdentifier, context );
	}

	/**
	 * @param checksum can be null
	 */
	public static void save( String requestUrl, File file, AmpConfig config, String checksum, Context context )
	{
		if ( checksum == null )
		{
			checksum = "sha256:" + HashUtils.getSha256( file );
		}
		FileCacheIndex cacheIndex = new FileCacheIndex( requestUrl, checksum );
		CacheIndexStore.save( requestUrl, cacheIndex, config, context );
	}
}
