package com.anfema.ionclient.utils;

import java.util.Collections;
import java.util.List;

public class ListUtils
{
	/**
	 * Sort a list of elements.
	 * <p>
	 * Method handles null objects before referring to T's own compare method
	 *
	 * @param elements the elements to be sorted of type T, where T implements {@link Comparable} interface
	 * @return simply reference to {@param elements}
	 */
	public static <T extends Comparable<T>> List<T> sort( List<T> elements )
	{
		Collections.sort( elements, ( lhs, rhs ) -> {
			// handle lhs object being null before passing to PagePreview's comparator
			if ( lhs == null && rhs == null )
			{
				return 0;
			}
			if ( lhs == null )
			{
				return 1;
			}
			if ( rhs == null )
			{
				return -1;
			}
			return lhs.compareTo( rhs );
		} );
		return elements;
	}
}
