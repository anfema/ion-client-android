package com.anfema.ionclient.pages.models.contents;

import android.graphics.Color;

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
}
