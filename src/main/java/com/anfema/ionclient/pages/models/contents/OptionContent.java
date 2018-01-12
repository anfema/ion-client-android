package com.anfema.ionclient.pages.models.contents;

import android.os.Parcel;

public class OptionContent extends Content
{
	public String value;

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel( Parcel dest, int flags )
	{
		super.writeToParcel( dest, flags );
		dest.writeString( this.value );
	}

	public OptionContent()
	{
	}

	protected OptionContent( Parcel in )
	{
		super( in );
		this.value = in.readString();
	}

	public static final Creator<OptionContent> CREATOR = new Creator<OptionContent>()
	{
		@Override
		public OptionContent createFromParcel( Parcel source )
		{
			return new OptionContent( source );
		}

		@Override
		public OptionContent[] newArray( int size )
		{
			return new OptionContent[ size ];
		}
	};
}
