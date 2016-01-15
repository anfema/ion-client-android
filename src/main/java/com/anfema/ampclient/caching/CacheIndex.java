package com.anfema.ampclient.caching;

import com.squareup.okhttp.HttpUrl;

/**
 * To save and retrieve file meta data, which is used to determine if the file in cache is up-to-date before requesting it.
 * Holds entries separated by collection, allowing to quickly clear the cache indices of a specific collection
 * (which needs to be done when the actual cache for that collection is cleared.)
 * <p/>
 * Uses shared preferences to save the data and {@link MemoryCacheIndex} for memory cache.
 */
public abstract class CacheIndex
{
	private String filename;

	public CacheIndex( String filename )
	{
		this.filename = filename;
	}

	/**
	 * Use MD5 of request URL as filename
	 */
	public CacheIndex( HttpUrl requestUrl )
	{
		this.filename = FilePaths.getFileName( requestUrl.toString() );
	}

	public String getFilename()
	{
		return filename;
	}

	public void setFilename( String filename )
	{
		this.filename = filename;
	}

}
