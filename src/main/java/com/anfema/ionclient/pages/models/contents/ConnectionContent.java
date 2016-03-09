package com.anfema.ionclient.pages.models.contents;


import android.net.Uri;

import java.util.List;

public class ConnectionContent extends Content
{
	public String       scheme;
	public String       collectionIdentifier;
	public List<String> pageIdentifierPath;
	public String       pageIdentifier;
	public String       contentIdentifier;

	public ConnectionContent( String connectionContentString )
	{
		this( null, connectionContentString );
	}

	public ConnectionContent( Content content, String connectionContentString )
	{
		if ( content != null )
		{
			outlet = content.outlet;
			variation = content.variation;
			position = content.position;
			is_searchable = content.is_searchable;
		}

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
			contentIdentifier = uri.getFragment();
		}
	}

	@Override
	public String toString()
	{
		return super.toString() + " + [scheme = " + scheme + ", collection = " + collectionIdentifier + ", page = " + pageIdentifier
				+ ", content = " + collectionIdentifier + "]";
	}

	@Override
	public boolean equals( Object other )
	{
		if ( !( other instanceof ConnectionContent ) )
		{
			return false;
		}

		ConnectionContent o = ( ConnectionContent ) other;
		return super.equals( other ) && equal( scheme, o.scheme ) && equal( collectionIdentifier, o.collectionIdentifier ) && equalPaths( o )
				&& equal( pageIdentifier, o.pageIdentifier ) && equal( contentIdentifier, o.contentIdentifier );
	}

	private boolean equalPaths( ConnectionContent o )
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
}
