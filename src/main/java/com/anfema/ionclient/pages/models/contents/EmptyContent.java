package com.anfema.ionclient.pages.models.contents;

import android.os.Parcel;

public class EmptyContent extends Content
{
	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel( Parcel dest, int flags )
	{
		super.writeToParcel( dest, flags );
	}

	public EmptyContent()
	{
	}

	protected EmptyContent( Parcel in )
	{
		super( in );
	}

	public static final Creator<EmptyContent> CREATOR = new Creator<EmptyContent>()
	{
		@Override
		public EmptyContent createFromParcel( Parcel source )
		{
			return new EmptyContent( source );
		}

		@Override
		public EmptyContent[] newArray( int size )
		{
			return new EmptyContent[ size ];
		}
	};
}
