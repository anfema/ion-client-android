package com.anfema.ionclient.pages.models.contents;

import android.os.Parcel;

@SuppressWarnings("unused")
public class FlagContent extends Content
{
	public boolean is_enabled;

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel( Parcel dest, int flags )
	{
		super.writeToParcel( dest, flags );
		dest.writeByte( this.is_enabled ? ( byte ) 1 : ( byte ) 0 );
	}

	public FlagContent()
	{
	}

	protected FlagContent( Parcel in )
	{
		super( in );
		this.is_enabled = in.readByte() != 0;
	}

	public static final Creator<FlagContent> CREATOR = new Creator<FlagContent>()
	{
		@Override
		public FlagContent createFromParcel( Parcel source )
		{
			return new FlagContent( source );
		}

		@Override
		public FlagContent[] newArray( int size )
		{
			return new FlagContent[ size ];
		}
	};
}
