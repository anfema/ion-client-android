package com.anfema.ionclient.pages;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.utils.FileUtils;

public class PagesUrls
{
	public static final String SLASH = FileUtils.SLASH;

	public static String getCollectionUrl( IonConfig config )
	{
		return config.baseUrl + config.locale + SLASH + config.collectionIdentifier;
	}

	public static String getPageUrl( IonConfig config, String pageId )
	{
		return config.baseUrl + config.locale + SLASH + config.collectionIdentifier + SLASH + pageId;
	}
}
