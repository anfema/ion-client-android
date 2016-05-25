package com.anfema.utils;

public class EqualsContract
{
	/**
	 * Checks if objects are equal. Handles {@code null} values and demands type identity.
	 *
	 * @return true if both objects are null or equal, false otherwise
	 */
	public static <T> boolean equal( T s1, T s2 )
	{
		// check for identity (and if both are null)
		if ( s1 == s2 )
		{
			return true;
		}
		// check for type identity and equality
		return s1 != null && s2 != null && s1.getClass().equals( s2.getClass() ) && s1.equals( s2 );
	}
}
