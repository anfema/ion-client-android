package com.anfema.utils;

public class StringUtils
{
	/**
	 * Flatten an iterable by concatenating the items into a single String using a separator.
	 *
	 * @param strings   an iterable with elements of type String
	 * @param separator a separator in-between each item
	 * @return flattened iterable to a single string
	 */
	public static String concatStrings( Iterable<String> strings, String separator )
	{
		if ( strings == null )
		{
			return null;
		}
		if ( separator == null )
		{
			separator = "";
		}
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for ( String s : strings )
		{
			sb.append( sep ).append( s );
			sep = separator;
		}
		return sb.toString();
	}

	/**
	 * Makes a ROUGH ESTIMATE about how much memory is required to store a string.
	 * <p>
	 * Since Java uses unicode, a character uses 2 bytes. Additional overhead is ignored here.
	 *
	 * @param string the string to be examined
	 * @return no. of bytes
	 */
	public static long byteCount( String string )
	{
		return string.length() * 2;
	}

}
