package com.anfema.ionclient.pages.models.contents;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Content implements Comparable<Content>, Parcelable
{
	public String variation;

	/**
	 * identifies the content, specifies where it is supposed to be used in a layout
	 */
	public String outlet;

	/**
	 * When there are more contents of same kind an array, position indicates their order.
	 */
	public long position;

	public boolean is_searchable; // not used by container outlet

	@Override
	public String toString()
	{
		return "Content [class: " + getClass().getSimpleName() + ", outlet = " + outlet + ", variation = " + variation + ", position = " + position
				+ ", is_searchable = " + is_searchable + "]";
	}

	/**
	 * Sort by positions ascending
	 */
	@Override
	public int compareTo( @NonNull Content another )
	{
		// Alternatively, one long could be subtracted from the other resulting in a long in the right range. However, casting to int might not be safe.
		if ( position == another.position )
		{
			return 0;
		}
		return position < another.position ? -1 : 1;
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel( Parcel dest, int flags )
	{
		dest.writeString( this.variation );
		dest.writeString( this.outlet );
		dest.writeLong( this.position );
		dest.writeByte( this.is_searchable ? ( byte ) 1 : ( byte ) 0 );
	}

	public Content()
	{
	}

	protected Content( Parcel in )
	{
		this.variation = in.readString();
		this.outlet = in.readString();
		this.position = in.readLong();
		this.is_searchable = in.readByte() != 0;
	}

	public static final Creator<Content> CREATOR = new Creator<Content>()
	{
		@Override
		public Content createFromParcel( Parcel in )
		{
			return new Content( in );
		}

		@Override
		public Content[] newArray( int size )
		{
			return new Content[ size ];
		}
	};
}