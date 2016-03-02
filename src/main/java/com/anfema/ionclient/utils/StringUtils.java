package com.anfema.ionclient.utils;

public class StringUtils
{
	/**
	 * Flatten a collection by concatenating the items into a single String using a separator.
	 *
	 * @param strings
	 * @param separator a separator in-between each item
	 * @return
	 */
	public static String concatStrings( Iterable<String> strings, String separator )
	{
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
}
