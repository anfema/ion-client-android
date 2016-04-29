package com.anfema.ionclient.utils;

import android.text.Html;

public class TextFormatting
{
	public static CharSequence format( String htmlOrPlain )
	{
		return containsHtmlTags( htmlOrPlain ) ? parseHtml( htmlOrPlain ) : htmlOrPlain;
	}

	public static boolean containsHtmlTags( String text )
	{
		String surroundedByTags = "<[a-z][\\s\\S]*>";
		return text.matches( surroundedByTags );
	}

	/**
	 * HTML parser wrapping Android's built-in parser and trims trailing whitespace artifacts at the end.
	 *
	 * @param htmlText text containing HTML tags (but not all tags are supported, e.g. no bullet points and image tags)
	 */
	public static CharSequence parseHtml( String htmlText )
	{
		return trimTrailingWhitespace( Html.fromHtml( htmlText ) );
	}

	/**
	 * Trims trailing whitespace. Removes any of these characters:
	 * 0009, HORIZONTAL TABULATION
	 * 000A, LINE FEED
	 * 000B, VERTICAL TABULATION
	 * 000C, FORM FEED
	 * 000D, CARRIAGE RETURN
	 * 001C, FILE SEPARATOR
	 * 001D, GROUP SEPARATOR
	 * 001E, RECORD SEPARATOR
	 * 001F, UNIT SEPARATOR
	 *
	 * @return "" if source is null, otherwise string with all trailing whitespace removed
	 * @see {http://stackoverflow.com/questions/9589381/remove-extra-line-breaks-after-html-fromhtml}
	 */
	private static CharSequence trimTrailingWhitespace( CharSequence source )
	{
		if ( source == null )
		{
			return "";
		}

		int i = source.length() - 1;

		// find first non-whitespace character
		while ( i >= 0 && Character.isWhitespace( source.charAt( i ) ) )
		{
			i--;
		}

		return source.subSequence( 0, i + 1 );
	}
}
