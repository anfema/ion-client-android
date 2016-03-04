package com.anfema.ionclient.pages.models.contents;


// TODO parse string into fields collectionIdentifier and pageIdentifier right at deserialization
public class ConnectionContent extends Content
{
	public String connection_string;

	public String getCollectionIdentifier()
	{
		return getCollectionIdentifier( connection_string );
	}

	public String getPageIdentifier()
	{
		return getPageIdentifier( connection_string );
	}

	public static String getCollectionIdentifier( String connectionContentString )
	{
		if ( connectionContentString == null )
		{
			return null;
		}

		if ( connectionContentString.length() <= 2 )
		{
			return connectionContentString;
		}

		String[] sections = connectionContentString.substring( 2 ).split( "/" );
		int index = sections.length - 2;
		if ( index < 0 )
		{
			index = 0;
		}
		return sections[ index ];
	}

	public static String getPageIdentifier( String connectionContentString )
	{
		if ( connectionContentString == null )
		{
			return null;
		}

		if ( connectionContentString.length() <= 2 )
		{
			return connectionContentString;
		}

		String[] sections = connectionContentString.substring( 2 ).split( "/" );
		return sections[ sections.length - 1 ];
	}
}
