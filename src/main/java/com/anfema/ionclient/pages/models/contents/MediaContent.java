package com.anfema.ionclient.pages.models.contents;

import android.os.Parcel;

@SuppressWarnings("unused")
public class MediaContent extends Content implements Downloadable
{
	public String original_checksum;

	public String file_size;

	public String width;

	public String checksum;

	public String original_file_size;

	public String original_width;

	public String original_mime_type;

	public String height;

	public String mime_type;

	public String original_file;

	public String file;

	public String name;

	public String length;

	public String original_height;

	public String original_length;

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
		dest.writeString( this.original_checksum );
		dest.writeString( this.file_size );
		dest.writeString( this.width );
		dest.writeString( this.checksum );
		dest.writeString( this.original_file_size );
		dest.writeString( this.original_width );
		dest.writeString( this.original_mime_type );
		dest.writeString( this.height );
		dest.writeString( this.mime_type );
		dest.writeString( this.original_file );
		dest.writeString( this.file );
		dest.writeString( this.name );
		dest.writeString( this.length );
		dest.writeString( this.original_height );
		dest.writeString( this.original_length );
	}

	public MediaContent()
	{
	}

	protected MediaContent( Parcel in )
	{
		super( in );
		this.original_checksum = in.readString();
		this.file_size = in.readString();
		this.width = in.readString();
		this.checksum = in.readString();
		this.original_file_size = in.readString();
		this.original_width = in.readString();
		this.original_mime_type = in.readString();
		this.height = in.readString();
		this.mime_type = in.readString();
		this.original_file = in.readString();
		this.file = in.readString();
		this.name = in.readString();
		this.length = in.readString();
		this.original_height = in.readString();
		this.original_length = in.readString();
	}

	public static final Creator<MediaContent> CREATOR = new Creator<MediaContent>()
	{
		@Override
		public MediaContent createFromParcel( Parcel source )
		{
			return new MediaContent( source );
		}

		@Override
		public MediaContent[] newArray( int size )
		{
			return new MediaContent[ size ];
		}
	};
}
