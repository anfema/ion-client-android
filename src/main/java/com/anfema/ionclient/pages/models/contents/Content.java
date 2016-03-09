package com.anfema.ionclient.pages.models.contents;

public class Content
{
	// TODO remove variation?
	public String variation;

	/**
	 * identifies the content, specifies where it is supposed to be used in a layout
	 */
	public String outlet;

	/**
	 * When there are more contents of same kind an array, position indicates their order.
	 */
	public long position;

	public boolean is_searchable; // not used by container outlet

	@Override
	public boolean equals( Object other )
	{
		if ( !( other instanceof Content ) )
		{
			return false;
		}

		Content o = ( Content ) other;
		return equal( outlet, o.outlet ) && equal( variation, o.variation ) && position == o.position && is_searchable == o.is_searchable;
	}

	protected boolean equal( String s1, String s2 )
	{
		if ( s1 == null )
		{
			return s2 == null;
		}
		return s1.equals( s2 );
	}

	@Override
	public String toString()
	{
		return "Content [class: " + getClass().getSimpleName() + ", outlet = " + outlet + ", variation = " + variation + ", position = " + position
				+ ", is_searchable = " + is_searchable + "]";
	}
}