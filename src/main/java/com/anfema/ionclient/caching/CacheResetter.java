package com.anfema.ionclient.caching;

import android.content.Context;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.caching.index.CacheIndexStore;
import com.anfema.ionclient.caching.index.MemoryCacheIndex;
import com.anfema.ionclient.utils.FileUtils;

import java.io.File;

public class CacheResetter
{

	public static void clear( Context context )
	{
		clearMemoryCache();
		clearFileCache( context );
	}

	public static void clearMemoryCache()
	{
		MemoryCache.clear();
		MemoryCacheIndex.clear();
	}

	public static void clearCollection( IonConfig config, Context context )
	{
		CacheIndexStore.clearCollection( config, context );
		MemoryCache.clear();
		clearCollectionFromFileCache( config, context );
	}

	private static void clearCollectionFromFileCache( IonConfig config, Context context )
	{
		File collectionFolderPath = FilePaths.getCollectionFolderPath( config, context );
		FileUtils.deleteRecursive( collectionFolderPath );
	}

	private static void clearFileCache( Context context )
	{
		CacheIndexStore.clear( context );
		FileUtils.deleteRecursive( FilePaths.getFilesDir( context ) );
	}
}
