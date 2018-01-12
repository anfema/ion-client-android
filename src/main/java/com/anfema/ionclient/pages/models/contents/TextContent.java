package com.anfema.ionclient.pages.models.contents;

import android.os.Parcel;

import com.anfema.utils.TextFormatting;

@SuppressWarnings("unused")
public class TextContent extends Content
{
	public static final String MIME_TYPE_TEXT_HTML = "text/html";

	public String text;

	public String mime_type;

	public String is_multiline;

	public CharSequence getTextFormatted()
	{
		if ( MIME_TYPE_TEXT_HTML.equals( mime_type ) )
		{
			return TextFormatting.parseHtml( text );
		}
		return text;
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
		dest.writeString( this.text );
		dest.writeString( this.mime_type );
		dest.writeString( this.is_multiline );
	}

	public TextContent()
	{
	}

	protected TextContent( Parcel in )
	{
		super( in );
		this.text = in.readString();
		this.mime_type = in.readString();
		this.is_multiline = in.readString();
	}

	public static final Creator<TextContent> CREATOR = new Creator<TextContent>()
	{
		@Override
		public TextContent createFromParcel( Parcel source )
		{
			return new TextContent( source );
		}

		@Override
		public TextContent[] newArray( int size )
		{
			return new TextContent[ size ];
		}
	};
}
