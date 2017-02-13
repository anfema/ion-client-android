package com.anfema.ionclient.pages.models.contents;

public class NumberContent extends Content
{
	private float value;
	private int   decimal_places;

	public float getValue()
	{
		return ( float ) ( value / Math.pow( 10, decimal_places ) );
	}
}
