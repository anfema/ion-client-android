package com.anfema.ionclient.pages;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.exceptions.NoIonPagesRequestException;
import com.anfema.ionclient.utils.FileUtils;

import java.util.regex.Pattern;

public class IonPageUrls
{
	public enum IonRequestType
	{
		COLLECTION, PAGE, MEDIA, ARCHIVE
	}

	public static final String SLASH           = FileUtils.SLASH;
	public static final String QUERY_BEGIN     = "?";
	public static final String QUERY_VARIATION = "variation=";

	private static final String[] MEDIA_URL_INDICATORS  = { "/media/", "/protected_media/" };
	private static final String   ARCHIVE_URL_INDICATOR = ".tar";

	public static IonRequestInfo analyze( String url, IonConfig config ) throws NoIonPagesRequestException
	{
		if ( isMediaRequestUrl( url ) )
		{
			return new IonRequestInfo( IonRequestType.MEDIA, null, null, null, null );
		}
		else if ( isArchiveUrl( url ) )
		{
			return new IonRequestInfo( IonRequestType.ARCHIVE, null, null, null, null );
		}
		else
		{
			String relativeUrlPath = url.replace( config.baseUrl, "" );
			String[] urlPathSegments = relativeUrlPath.split( SLASH );

			if ( urlPathSegments.length < 2 || urlPathSegments.length > 3 )
			{
				throw new NoIonPagesRequestException( url );
			}
			String[] idPlusVariation = urlPathSegments[ urlPathSegments.length - 1 ].split( Pattern.quote( "?" ) );
			String locale;
			String variation;
			if ( idPlusVariation.length == 2 )
			{
				locale = urlPathSegments[ 0 ];
				variation = idPlusVariation[ 1 ];
			}
			else if ( idPlusVariation.length == 1 )
			{
				locale = urlPathSegments[ 0 ];
				variation = IonConfig.DEFAULT_VARIATION;
			}
			else
			{
				throw new NoIonPagesRequestException( url );
			}

			String collectionIdentifier;

			if ( urlPathSegments.length == 2 )
			{
				collectionIdentifier = idPlusVariation[ 0 ];
				return new IonRequestInfo( IonRequestType.COLLECTION, locale, variation, collectionIdentifier, null );
			}
			else // urlPathSegments.length == 3
			{
				collectionIdentifier = urlPathSegments[ 1 ];
				String pageIdentifier = idPlusVariation[ 0 ];
				return new IonRequestInfo( IonRequestType.PAGE, locale, variation, collectionIdentifier, pageIdentifier );
			}
		}
	}

	public static boolean isMediaRequestUrl( String url )
	{
		for ( String mediaIndicator : MEDIA_URL_INDICATORS )
		{
			if ( url.contains( mediaIndicator ) )
			{
				return true;
			}
		}
		return false;
	}

	private static boolean isArchiveUrl( String url )
	{
		return url.endsWith( ARCHIVE_URL_INDICATOR ) || url.contains( ARCHIVE_URL_INDICATOR + "?" );
	}

	public static String getCollectionUrl( IonConfig config )
	{
		return config.baseUrl + config.locale + SLASH + config.collectionIdentifier + QUERY_BEGIN + QUERY_VARIATION + config.variation;
	}

	public static String getPageUrl( IonConfig config, String pageId )
	{
		return config.baseUrl + config.locale + SLASH + config.collectionIdentifier + SLASH + pageId + QUERY_BEGIN + QUERY_VARIATION + config.variation;
	}

	public static class IonRequestInfo
	{
		public IonRequestType requestType;
		public String         locale;
		public String         variation;
		public String         collectionIdentifier;
		public String         pageIdentifier;

		public IonRequestInfo( IonRequestType requestType, String locale, String variation, String collectionIdentifier, String pageIdentifier )
		{
			this.requestType = requestType;
			this.locale = locale;
			this.variation = variation;
			this.collectionIdentifier = collectionIdentifier;
			this.pageIdentifier = pageIdentifier;
		}
	}
}
