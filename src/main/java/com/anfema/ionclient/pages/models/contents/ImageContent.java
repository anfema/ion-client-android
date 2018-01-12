package com.anfema.ionclient.pages.models.contents;

import android.os.Parcel;

@SuppressWarnings("unused")
public class ImageContent extends Content implements Downloadable
{
	public String mime_type;

	public String image;

	public int width;

	public int height;

	public int file_size;

	public String checksum;


	public String original_mime_type;

	public String original_image;

	public int original_width;

	public int original_height;

	public int original_file_size;

	public String original_checksum;


	public int translation_x;

	public int translation_y;

	public float scale;

	@Override
	public String getUrl()
	{
		return image;
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
		dest.writeString( this.mime_type );
		dest.writeString( this.image );
		dest.writeInt( this.width );
		dest.writeInt( this.height );
		dest.writeInt( this.file_size );
		dest.writeString( this.checksum );
		dest.writeString( this.original_mime_type );
		dest.writeString( this.original_image );
		dest.writeInt( this.original_width );
		dest.writeInt( this.original_height );
		dest.writeInt( this.original_file_size );
		dest.writeString( this.original_checksum );
		dest.writeInt( this.translation_x );
		dest.writeInt( this.translation_y );
		dest.writeFloat( this.scale );
	}

	public ImageContent()
	{
	}

	protected ImageContent( Parcel in )
	{
		super( in );
		this.mime_type = in.readString();
		this.image = in.readString();
		this.width = in.readInt();
		this.height = in.readInt();
		this.file_size = in.readInt();
		this.checksum = in.readString();
		this.original_mime_type = in.readString();
		this.original_image = in.readString();
		this.original_width = in.readInt();
		this.original_height = in.readInt();
		this.original_file_size = in.readInt();
		this.original_checksum = in.readString();
		this.translation_x = in.readInt();
		this.translation_y = in.readInt();
		this.scale = in.readFloat();
	}

	public static final Creator<ImageContent> CREATOR = new Creator<ImageContent>()
	{
		@Override
		public ImageContent createFromParcel( Parcel source )
		{
			return new ImageContent( source );
		}

		@Override
		public ImageContent[] newArray( int size )
		{
			return new ImageContent[ size ];
		}
	};
}
