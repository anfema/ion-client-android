package com.anfema.ionclient.pages.models.contents;

import android.os.Parcel;

import java.util.ArrayList;

public class ContainerContent extends Content
{
	public ArrayList<Content> children;

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel( Parcel dest, int flags )
	{
		super.writeToParcel( dest, flags );
		// TODO write logic to parcel children. (Save element + type)
	}

	public ContainerContent()
	{
	}

	protected ContainerContent( Parcel in )
	{
		super( in );
		// TODO write logic to read children. (recover type + element)
	}

	public static final Creator<ContainerContent> CREATOR = new Creator<ContainerContent>()
	{
		@Override
		public ContainerContent createFromParcel( Parcel source )
		{
			return new ContainerContent( source );
		}

		@Override
		public ContainerContent[] newArray( int size )
		{
			return new ContainerContent[ size ];
		}
	};
}
