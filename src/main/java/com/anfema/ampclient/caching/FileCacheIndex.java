package com.anfema.ampclient.caching;

import android.content.Context;

import com.squareup.okhttp.HttpUrl;

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

	// Persistence - shared preferences

	public static FileCacheIndex retrieve( String requestUrl, Context context )
	{
		return CacheIndex.retrieve( requestUrl, context, FileCacheIndex.class );
	}
}