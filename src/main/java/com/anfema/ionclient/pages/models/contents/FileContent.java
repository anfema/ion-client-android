package com.anfema.ionclient.pages.models.contents;

import android.os.Parcel;

@SuppressWarnings("unused")
public class FileContent extends Content implements Downloadable
{
	public String file_size;

	public String mime_type;

	public String file;

	public String name;

	public String checksum;

	@Override
	public String getUrl()
	{
		return file;
	}

	@Override
	public String getChecksum()
	{
		return checksum;
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
		dest.writeString( this.file_size );
		dest.writeString( this.mime_type );
		dest.writeString( this.file );
		dest.writeString( this.name );
		dest.writeString( this.checksum );
	}

	public FileContent()
	{
	}

	protected FileContent( Parcel in )
	{
		super( in );
		this.file_size = in.readString();
		this.mime_type = in.readString();
		this.file = in.readString();
		this.name = in.readString();
		this.checksum = in.readString();
	}

	public static final Creator<FileContent> CREATOR = new Creator<FileContent>()
	{
		@Override
		public FileContent createFromParcel( Parcel source )
		{
			return new FileContent( source );
		}

		@Override
		public FileContent[] newArray( int size )
		{
			return new FileContent[ size ];
		}
	};
}