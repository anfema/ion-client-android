package com.anfema.ampclient.pages;

import com.anfema.ampclient.utils.FileUtils;
import com.squareup.okhttp.HttpUrl;

public enum AmpCallType
{
	COLLECTIONS( "collections" ), PAGES( "pages" );

	private final String pathSegment;

	AmpCallType( String pathSegment )
	{

		this.pathSegment = pathSegment;
	}

	@Override
	public String toString()
	{
		return pathSegment;
	}

	public static AmpCallType determineCall( String stringRepresentation ) throws IllegalArgumentException
	{
		for ( AmpCallType ampCallType : AmpCallType.values() )
		{
			if ( ampCallType.toString().equals( stringRepresentation ) )
			{
				return ampCallType;
			}
		}
		throw new IllegalArgumentException( "No AmpCall found for " + stringRepresentation );
	}

	public static AmpCallType determineCall( HttpUrl httpUrl )
	{
		String url = httpUrl.toString();
		for ( AmpCallType ampCallType : AmpCallType.values() )
		{
			if ( url.contains( FileUtils.SLASH + ampCallType.toString() + FileUtils.SLASH ) )
			{
				return ampCallType;
			}
		}
		throw new IllegalArgumentException( "No AmpCall could be determined for " + url );
	}
}
