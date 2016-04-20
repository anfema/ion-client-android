package com.anfema.ionclient.pages.models.contents;


import android.net.Uri;

import java.util.Arrays;
import java.util.List;

public class Connection
{
	public String       scheme;
	public String       collectionIdentifier;
	public List<String> pageIdentifierPath;
	public String       pageIdentifier;
	public String       outletIdentifier;

	public Connection( String connectionContentString )
	{
		if ( connectionContentString != null )
		{
			Uri uri = Uri.parse( connectionContentString );
			scheme = uri.getScheme();
			collectionIdentifier = uri.getHost();
			pageIdentifierPath = uri.getPathSegments();
			if ( pageIdentifierPath != null && !pageIdentifierPath.isEmpty() )
			{
				pageIdentifier = pageIdentifierPath.get( pageIdentifierPath.size() - 1 );
			}
			outletIdentifier = uri.getFragment();
		}
	}

	@Override
	public String toString()
	{
		return "Connection [scheme = " + scheme + ", collection = " + collectionIdentifier + ", page = " + pageIdentifier
				+ ", outlet = " + outletIdentifier + "]";
	}

	@Override
	public boolean equals( Object other )
	{
		if ( !( other instanceof Connection ) )
		{
			return false;
		}

		Connection o = ( Connection ) other;
		return equal( scheme, o.scheme ) && equal( collectionIdentifier, o.collectionIdentifier ) && equalPaths( o )
				&& equal( pageIdentifier, o.pageIdentifier ) && equal( outletIdentifier, o.outletIdentifier );
	}

	protected boolean equal( String s1, String s2 )
	{
		if ( s1 == null )
		{
			return s2 == null;
		}
		return s1.equals( s2 );
	}

	private boolean equalPaths( Connection o )
	{
		if ( pageIdentifierPath == null )
		{
			return o.pageIdentifierPath == null;
		}
		if ( pageIdentifierPath.size() != o.pageIdentifierPath.size() )
		{
			return false;
		}
		for ( int i = 0; i < pageIdentifierPath.size(); i++ )
		{
			String page = pageIdentifierPath.get( i );
			String otherPage = o.pageIdentifierPath.get( i );
			if ( !equal( page, otherPage ) )
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode()
	{
		Object[] hashRelevantFields = { scheme, collectionIdentifier, pageIdentifierPath, pageIdentifier, outletIdentifier };
		return Arrays.hashCode( hashRelevantFields );
	}
}
