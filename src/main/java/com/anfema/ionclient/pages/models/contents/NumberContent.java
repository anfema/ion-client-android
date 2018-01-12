package com.anfema.ionclient.pages.models.contents;

import android.os.Parcel;

public class NumberContent extends Content
{
	private float value;
	private int   decimal_places;

	public float getValue()
	{
		return ( float ) ( value / Math.pow( 10, decimal_places ) );
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
		dest.writeFloat( this.value );
		dest.writeInt( this.decimal_places );
	}

	public NumberContent()
	{
	}

	protected NumberContent( Parcel in )
	{
		super( in );
		this.value = in.readFloat();
		this.decimal_places = in.readInt();
	}

	public static final Creator<NumberContent> CREATOR = new Creator<NumberContent>()
	{
		@Override
		public NumberContent createFromParcel( Parcel source )
		{
			return new NumberContent( source );
		}

		@Override
		public NumberContent[] newArray( int size )
		{
			return new NumberContent[ size ];
		}
	};
}
