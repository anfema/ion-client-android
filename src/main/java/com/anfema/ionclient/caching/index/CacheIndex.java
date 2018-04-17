package com.anfema.ionclient.caching.index;

/**
 * To save and retrieve file meta data, which is used to determine if the file in cache is up-to-date before requesting it.
 * Holds entries separated by collection, allowing to quickly clear the cache indices of a specific collection
 * (which needs to be done when the actual cache for that collection is cleared.)
 * <p>
 * Uses shared preferences to save the data.
 */
public abstract class CacheIndex
{
	public transient int byteCount;
}
