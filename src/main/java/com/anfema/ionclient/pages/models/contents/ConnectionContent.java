package com.anfema.ionclient.pages.models.contents;


import java.util.List;

public class ConnectionContent extends Content
{
	public Connection connection;

	public ConnectionContent( String connectionContentString )
	{
		this( null, connectionContentString );
	}

	public ConnectionContent( Content content, String connectionContentString )
	{
		this( content, new Connection( connectionContentString ) );
	}

	public ConnectionContent( Content content, Connection connection )
	{
		if ( content != null )
		{
			outlet = content.outlet;
			variation = content.variation;
			position = content.position;
			is_searchable = content.is_searchable;
		}
		this.connection = connection;
	}

	public String getScheme()
	{
		return connection.scheme;
	}

	public String getCollectionIdentifier()
	{
		return connection.collectionIdentifier;
	}

	public List<String> getPageIdentifierPath()
	{
		return connection.pageIdentifierPath;
	}

	public String getPageIdentifier()
	{
		return connection.pageIdentifier;
	}

	public String getContentIdentifier()
	{
		return connection.contentIdentifier;
	}

	@Override
	public String toString()
	{
		String toString = super.toString();
		if ( connection != null )
		{
			toString += " + [scheme = " + connection.scheme + ", collection = " + connection.collectionIdentifier + ", page = " + connection.pageIdentifier
					+ ", content = " + connection.collectionIdentifier + "]";
		}
		return toString;
	}
}
