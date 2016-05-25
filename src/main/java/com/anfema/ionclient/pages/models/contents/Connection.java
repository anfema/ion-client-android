package com.anfema.ionclient.pages.models.contents;


import android.net.Uri;

import com.anfema.utils.EqualsContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Connection
{
	public final String       scheme;
	public final String       collectionIdentifier;
	public final List<String> pageIdentifierPath;
	public final String       pageIdentifier;
	public final String       outletIdentifier;

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
			else
			{
				pageIdentifier = null;
			}
			outletIdentifier = uri.getFragment();
		}
		else
		{
			scheme = null;
			collectionIdentifier = null;
			pageIdentifierPath = new ArrayList<>();
			pageIdentifier = null;
			outletIdentifier = null;
		}
	}

	public Connection( String scheme, String collectionIdentifier, String pageIdentifier, String outletIdentifier )
	{
		this.scheme = scheme;
		this.collectionIdentifier = collectionIdentifier;
		this.pageIdentifier = pageIdentifier;
		this.pageIdentifierPath = new ArrayList<>();
		if ( pageIdentifier != null )
		{
			pageIdentifierPath.add( pageIdentifier );
		}
		this.outletIdentifier = outletIdentifier;
	}

	public Connection( String scheme, String collectionIdentifier, List<String> pageIdentifierPath, String outletIdentifier )
	{
		this.scheme = scheme;
		this.collectionIdentifier = collectionIdentifier;
		this.pageIdentifierPath = pageIdentifierPath;
		if ( pageIdentifierPath != null && !pageIdentifierPath.isEmpty() )
		{
			pageIdentifier = pageIdentifierPath.get( pageIdentifierPath.size() - 1 );
		}
		else
		{
			pageIdentifier = null;
		}
		this.outletIdentifier = outletIdentifier;
	}

	@Override
	public String toString()
	{
		return "Connection [scheme = " + scheme + ", collection = " + collectionIdentifier + ", page = " + pageIdentifier
				+ ", outlet = " + outletIdentifier + "]";
	}

	@Override
	public final boolean equals( Object other )
	{
		if ( !( other instanceof Connection ) )
		{
			return false;
		}

		Connection o = ( Connection ) other;
		return EqualsContract.equal( scheme, o.scheme ) && EqualsContract.equal( collectionIdentifier, o.collectionIdentifier ) && equalPaths( o )
				&& EqualsContract.equal( pageIdentifier, o.pageIdentifier ) && EqualsContract.equal( outletIdentifier, o.outletIdentifier );
	}

	private boolean equalPaths( Connection o )
	{
		if ( pageIdentifierPath == null )
		{
			return o.pageIdentifierPath == null;
		}
		if ( o.pageIdentifierPath == null )
		{
			return false;
		}
		if ( pageIdentifierPath.size() != o.pageIdentifierPath.size() )
		{
			return false;
		}
		for ( int i = 0; i < pageIdentifierPath.size(); i++ )
		{
			String page = pageIdentifierPath.get( i );
			String otherPage = o.pageIdentifierPath.get( i );
			if ( !EqualsContract.equal( page, otherPage ) )
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public final int hashCode()
	{
		Object[] hashRelevantFields = { scheme, collectionIdentifier, pageIdentifierPath, pageIdentifier, outletIdentifier };
		return Arrays.hashCode( hashRelevantFields );
	}
}
