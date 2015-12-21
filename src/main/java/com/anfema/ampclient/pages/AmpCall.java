package com.anfema.ampclient.pages;

import com.anfema.ampclient.utils.FileUtils;
import com.squareup.okhttp.HttpUrl;

public enum AmpCall
{
	COLLECTIONS( "collections" ), PAGES( "pages" );

	private final String pathSegment;

	AmpCall( String pathSegment )
	{

		this.pathSegment = pathSegment;
	}

	@Override
	public String toString()
	{
		return pathSegment;
	}

	public static AmpCall fromPathSegment( String pathSegment ) throws IllegalArgumentException
	{
		for ( AmpCall ampCall : AmpCall.values() )
		{
			if ( ampCall.toString().equals( pathSegment ) )
			{
				return ampCall;
			}
		}
		throw new IllegalArgumentException( "No AmpCall found for " + pathSegment );
	}

	public static AmpCall determineCall( HttpUrl httpUrl )
	{
		String url = httpUrl.toString();
		for ( AmpCall ampCall : AmpCall.values() )
		{
			if ( url.contains( FileUtils.SLASH + ampCall.toString() + FileUtils.SLASH ) )
			{
				return ampCall;
			}
		}
		throw new IllegalArgumentException( "No AmpCall could be determined for " + url );
	}
}
