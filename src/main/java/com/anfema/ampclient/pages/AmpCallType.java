package com.anfema.ampclient.pages;

import com.anfema.ampclient.exceptions.NoAmpPagesRequestException;
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

	public static AmpCallType determineCall( String callTypeString ) throws NoAmpPagesRequestException
	{
		for ( AmpCallType ampCallType : AmpCallType.values() )
		{
			if ( ampCallType.toString().equals( callTypeString ) )
			{
				return ampCallType;
			}
		}
		throw new NoAmpPagesRequestException( "No AmpCall found for " + callTypeString );
	}

	public static AmpCallType determineCall( HttpUrl httpUrl ) throws NoAmpPagesRequestException
	{
		String url = httpUrl.toString();
		for ( AmpCallType ampCallType : AmpCallType.values() )
		{
			if ( url.contains( FileUtils.SLASH + ampCallType.toString() + FileUtils.SLASH ) )
			{
				return ampCallType;
			}
		}
		throw new NoAmpPagesRequestException( "No AMP pages call could be determined for " + url );
	}
}
