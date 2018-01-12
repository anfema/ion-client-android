package com.anfema.ionclient.pages.models.contents;

import android.graphics.Color;
import android.os.Parcel;

/**
 * Color components r, g, b, a are expected required to be in range 0..255
 */
public class ColorContent extends Content
{
	public int r;

	public int g;

	public int b;

	public int a;

	/**
	 * @return ARGB color code
	 * @see Color#argb(int, int, int, int)
	 */
	public int getColor()
	{
		return Color.argb( a, r, g, b );
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
		dest.writeInt( this.r );
		dest.writeInt( this.g );
		dest.writeInt( this.b );
		dest.writeInt( this.a );
	}

	public ColorContent()
	{
	}

	protected ColorContent( Parcel in )
	{
		super( in );
		this.r = in.readInt();
		this.g = in.readInt();
		this.b = in.readInt();
		this.a = in.readInt();
	}

	public static final Creator<ColorContent> CREATOR = new Creator<ColorContent>()
	{
		@Override
		public ColorContent createFromParcel( Parcel source )
		{
			return new ColorContent( source );
		}

		@Override
		public ColorContent[] newArray( int size )
		{
			return new ColorContent[ size ];
		}
	};
}
