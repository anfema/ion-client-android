package com.anfema.ionclient.pages;

import com.anfema.ionclient.pages.models.Collection;

public interface CollectionDownloadedListener
{
	void collectionDownloaded( Collection collection, String lastModified );
}
