package com.anfema.ionclient.utils;

import android.app.ActivityManager;
import android.content.Context;

import com.anfema.utils.Log;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB;

public class MemoryUtils
{
	/**
	 * @return app's total memory cache size in bytes
	 */
	public static int calculateAvailableMemCache( Context context )
	{
		ActivityManager am = ( ActivityManager ) context.getSystemService( ACTIVITY_SERVICE );
		boolean largeHeap = ( context.getApplicationInfo().flags & FLAG_LARGE_HEAP ) != 0;
		int memoryClass = largeHeap && SDK_INT >= HONEYCOMB ? am.getLargeMemoryClass() : am.getMemoryClass();
		IonLog.i( "Memory Cache", "available Cache: " + memoryClass + " MB" );
		return 1024 * 1024 * memoryClass;
	}
}
