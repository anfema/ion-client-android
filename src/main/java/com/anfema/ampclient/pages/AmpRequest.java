package com.anfema.ampclient.pages;

import com.anfema.ampclient.AmpConfig;
import com.anfema.ampclient.exceptions.NoAmpPagesRequestException;
import com.anfema.ampclient.utils.FileUtils;

import okhttp3.HttpUrl;

public enum AmpRequest
{
	COLLECTION, PAGE, MEDIA;

	public static final String SLASH = "/";

	private static final String[] MEDIA_URL_INDICATORS = { "/media/", "/protected_media/" };

	public static AmpRequest determineCall( HttpUrl httpUrl, AmpConfig config ) throws NoAmpPagesRequestException
	{
		String url = httpUrl.toString();
		for ( AmpRequest ampRequest : AmpRequest.values() )
		{
			if ( url.contains( FileUtils.SLASH + ampRequest.toString() + FileUtils.SLASH ) )
			{
				return ampRequest;
			}
		}
		throw new NoAmpPagesRequestException( "No AMP pages call could be determined for " + url );
	}

	public static AmpRequestInfo analyze( String url, AmpConfig config ) throws NoAmpPagesRequestException
	{
		if ( isMediaRequestUrl( url ) )
		{
			return new AmpRequestInfo( MEDIA, null, null, null );
		}
		else
		{
			String relativeUrlPath = url.replace( config.baseUrl, "" );
			String[] urlPathSegments = relativeUrlPath.split( "/" );

			// urlPathSegments length is 2 for collection call and 3 for page call
			if ( urlPathSegments.length < 2 || urlPathSegments.length > 3 )
			{
				throw new NoAmpPagesRequestException( url );
			}

			if ( urlPathSegments.length == 2 )
			{
				return new AmpRequestInfo( COLLECTION, urlPathSegments[ 0 ], urlPathSegments[ 1 ], null );
			}
			else if ( urlPathSegments.length == 3 )
			{
				return new AmpRequestInfo( PAGE, urlPathSegments[ 0 ], urlPathSegments[ 1 ], urlPathSegments[ 2 ] );
			}
			else
			{
				throw new NoAmpPagesRequestException( url );
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

	public static String getCollectionUrl( AmpConfig config )
	{
		return config.baseUrl + config.locale + SLASH + config.collectionIdentifier;
	}

	public static String getPageUrl( AmpConfig config, String pageId )
	{
		return config.baseUrl + config.locale + SLASH + config.collectionIdentifier + SLASH + pageId;
	}

	public static class AmpRequestInfo
	{
		public AmpRequest requestType;
		public String     locale;
		public String     collectionIdentifier;
		public String     pageIdentifier;

		public AmpRequestInfo( AmpRequest requestType, String locale, String collectionIdentifier, String pageIdentifier )
		{
			this.requestType = requestType;
			this.locale = locale;
			this.collectionIdentifier = collectionIdentifier;
			this.pageIdentifier = pageIdentifier;
		}
	}
}
