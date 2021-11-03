package com.anfema.ionclient.pages.models.contents;


import android.os.Parcel;

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
			index = content.index;
			is_searchable = content.is_searchable;
		}
		this.connection = connection;
	}

	@Override
	public String toString()
	{
		String toString = super.toString();
		if ( connection != null )
		{
			toString += connection.toString();
		}
		return toString;
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel( Parcel dest, int flags )
	{
		super.writeToParcel( dest, flags );
		dest.writeParcelable( this.connection, flags );
	}

	protected ConnectionContent( Parcel in )
	{
		super( in );
		this.connection = in.readParcelable( Connection.class.getClassLoader() );
	}

	public static final Creator<ConnectionContent> CREATOR = new Creator<ConnectionContent>()
	{
		@Override
		public ConnectionContent createFromParcel( Parcel source )
		{
			return new ConnectionContent( source );
		}

		@Override
		public ConnectionContent[] newArray( int size )
		{
			return new ConnectionContent[ size ];
		}
	};
}
