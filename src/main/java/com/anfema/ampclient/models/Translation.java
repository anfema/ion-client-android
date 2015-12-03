package com.anfema.ampclient.models;

import com.anfema.ampclient.models.contents.AContent;

import java.util.ArrayList;

/**
 * A page can be published for different languages. Every language has its own contents.
 */
public class Translation
{
	/**
	 * e.g. "de_DE"
	 */
	public String locale;

	/**
	 * Only one item, which is of type "containercontent", holding all contents of page in nested form.
	 */
	private ArrayList<AContent> content;

	public AContent getContent()
	{
		if ( content == null )
		{
			return null;
		}
		return content.get( 0 );
	}

	@Override
	public String toString()
	{
		return "Translation [content = " + content + ", locale = " + locale + "]";
	}
}
