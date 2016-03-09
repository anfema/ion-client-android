package com.anfema.ionclient.pages;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.exceptions.NoIonPagesRequestException;
import com.anfema.ionclient.utils.FileUtils;

import okhttp3.HttpUrl;

public enum IonRequest
{
	COLLECTION, PAGE, MEDIA;

	public static final String SLASH = "/";

	private static final String[] MEDIA_URL_INDICATORS = { "/media/", "/protected_media/" };

	public static IonRequest determineCall( HttpUrl httpUrl, IonConfig config ) throws NoIonPagesRequestException
	{
		String url = httpUrl.toString();
		for ( IonRequest ionRequest : IonRequest.values() )
		{
			if ( url.contains( FileUtils.SLASH + ionRequest.toString() + FileUtils.SLASH ) )
			{
				return ionRequest;
			}
		}
		throw new NoIonPagesRequestException( "No ION pages call could be determined for " + url );
	}

	public static IonRequestInfo analyze( String url, IonConfig config ) throws NoIonPagesRequestException
	{
		if ( isMediaRequestUrl( url ) )
		{
			return new IonRequestInfo( MEDIA, null, null, null );
		}
		else
		{
			String relativeUrlPath = url.replace( config.baseUrl, "" );
			String[] urlPathSegments = relativeUrlPath.split( "/" );

			switch ( urlPathSegments.length )
			{
				// urlPathSegments length is 2 for collection call and 3 for page call
				case 2:
					return new IonRequestInfo( COLLECTION, urlPathSegments[ 0 ], urlPathSegments[ 1 ], null );
				case 3:
					return new IonRequestInfo( PAGE, urlPathSegments[ 0 ], urlPathSegments[ 1 ], urlPathSegments[ 2 ] );
				default:
					throw new NoIonPagesRequestException( url );
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

	public static String getCollectionUrl( IonConfig config )
	{
		return config.baseUrl + config.locale + SLASH + config.collectionIdentifier;
	}

	public static String getPageUrl( IonConfig config, String pageId )
	{
		return config.baseUrl + config.locale + SLASH + config.collectionIdentifier + SLASH + pageId;
	}

	public static class IonRequestInfo
	{
		public IonRequest requestType;
		public String     locale;
		public String     collectionIdentifier;
		public String     pageIdentifier;

		public IonRequestInfo( IonRequest requestType, String locale, String collectionIdentifier, String pageIdentifier )
		{
			this.requestType = requestType;
			this.locale = locale;
			this.collectionIdentifier = collectionIdentifier;
			this.pageIdentifier = pageIdentifier;
		}
	}
}
