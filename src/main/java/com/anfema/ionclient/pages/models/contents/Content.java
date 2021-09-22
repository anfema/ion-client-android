package com.anfema.ionclient.pages.models.contents;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Content implements Comparable<Content>, Parcelable
{
	public String variation;

	/**
	 * identifies the content, specifies where it is supposed to be used in a layout
	 */
	public String outlet;

	/**
	 * Non-null if the Content is a list element within a {@link ContainerContent}
	 */
	@Nullable
	public Long index;

	public boolean is_searchable; // not used by container outlet

	@Override
	public String toString()
	{
		return "Content [class: " + getClass().getSimpleName() + ", outlet = " + outlet
				+ ", variation = " + variation + ", index = " + index
				+ ", is_searchable = " + is_searchable + "]";
	}

	/**
	 * Sort by positions ascending
	 */
	@Override
	public int compareTo( @NonNull Content another )
	{
		if ( Objects.equals( index, another.index ) )
		{
			return 0;
		}
		else if ( index == null )
		{
			return 1;
		}
		else if ( another.index == null )
		{
			return -1;
		}
		else
		{
			return index < another.index ? -1 : 1;
		}
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
		dest.writeSerializable( this.index );
		dest.writeByte( this.is_searchable ? ( byte ) 1 : ( byte ) 0 );
	}

	public Content()
	{
	}

	protected Content( Parcel in )
	{
		this.variation = in.readString();
		this.outlet = in.readString();
		this.index = ( Long ) in.readSerializable();
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
