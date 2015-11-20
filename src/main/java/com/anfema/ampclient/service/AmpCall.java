package com.anfema.ampclient.service;

import com.squareup.okhttp.HttpUrl;

import java.util.HashSet;

public enum AmpCall
{
	COLLECTIONS( "collections" ), PAGES( "pages" ), AUTHENTICATE( "login" );

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

	public static boolean isLoginRequest( HttpUrl url )
	{
		boolean containsLogin = url.encodedPath().contains( AUTHENTICATE.toString() );
		if ( !containsLogin )
		{
			return false;
		}

		HashSet<String> loginParameters = new HashSet<>();
		loginParameters.add( "username" );
		loginParameters.add( "password" );
		boolean containsParameters = url.queryParameterNames().containsAll( loginParameters );
		return containsParameters;
	}
}
