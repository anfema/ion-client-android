package com.anfema.ionclient.pages.models.contents;

import android.os.Parcel;

import org.joda.time.DateTime;

public class DatetimeContent extends Content
{
	public DateTime datetime;

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel( Parcel dest, int flags )
	{
		super.writeToParcel( dest, flags );
		dest.writeSerializable( this.datetime );
	}

	public DatetimeContent()
	{
	}

	protected DatetimeContent( Parcel in )
	{
		super( in );
		this.datetime = ( DateTime ) in.readSerializable();
	}

	public static final Creator<DatetimeContent> CREATOR = new Creator<DatetimeContent>()
	{
		@Override
		public DatetimeContent createFromParcel( Parcel source )
		{
			return new DatetimeContent( source );
		}

		@Override
		public DatetimeContent[] newArray( int size )
		{
			return new DatetimeContent[ size ];
		}
	};
}
