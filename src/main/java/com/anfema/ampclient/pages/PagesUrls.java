package com.anfema.ampclient.pages;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.utils.FileUtils;

public class PagesUrls
{
	public static String getCollectionUrl( AmpConfig config )
	{
		return config.baseUrl + AmpCallType.COLLECTIONS.toString() + FileUtils.SLASH + config.collectionIdentifier;
	}

	public static String getPageUrl( AmpConfig config, String pageId )
	{
		return config.baseUrl + AmpCallType.PAGES.toString() + FileUtils.SLASH + config.collectionIdentifier + FileUtils.SLASH + pageId;
	}
}