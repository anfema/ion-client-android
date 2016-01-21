package com.anfema.ampclient.pages;

import com.anfema.ampclient.caching.CollectionCacheIndex;
import com.anfema.ampclient.pages.models.Collection;

public interface CollectionDownloadedListener
{
	void collectionDownloaded( Collection collection, String lastModified );
}
