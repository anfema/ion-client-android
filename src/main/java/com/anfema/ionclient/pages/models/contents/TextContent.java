package com.anfema.ionclient.pages.models.contents;

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
}
