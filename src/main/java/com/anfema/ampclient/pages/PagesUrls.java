package com.anfema.ampclient.pages;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.utils.FileUtils;

public class PagesUrls
{
	public static final String SLASH = FileUtils.SLASH;

	public static String getCollectionUrl( AmpConfig config )
	{
		return config.baseUrl + config.locale + SLASH + config.collectionIdentifier;
	}

	public static String getPageUrl( AmpConfig config, String pageId )
	{
		return config.baseUrl + config.locale + SLASH + config.collectionIdentifier + SLASH + pageId;
	}
}
