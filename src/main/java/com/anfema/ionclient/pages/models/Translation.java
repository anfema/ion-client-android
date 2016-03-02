package com.anfema.ionclient.pages.models;

import com.anfema.ionclient.pages.models.contents.IContent;

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
	private ArrayList<IContent> content;

	public IContent getContent()
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
