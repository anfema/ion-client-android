package com.anfema.ionclient.utils;

import com.anfema.ionclient.pages.models.PagePreview;

import java.util.List;

import rx.functions.Func1;

/**
 * This utility filters are supposed to be used with {@link com.anfema.ionclient.pages.IonPages#fetchPagePreviews(Func1)}
 * and {@link com.anfema.ionclient.pages.IonPages#fetchPages(Func1)}.
 */
public class PagesFilter
{
	/**
	 * Fetch all (preview) pages of current collection.
	 */
	public static final Func1<PagePreview, Boolean> ALL = pagePreview -> pagePreview != null;

	/**
	 * Fetch all (preview) pages, which are (tree-hierarchical) root pages within the collection.
	 */
	public static final Func1<PagePreview, Boolean> ROOT_ELEMENTS = pagePreview -> pagePreview != null && pagePreview.parent == null;

	/**
	 * Before using this filter method, consider using
	 * {@link com.anfema.ionclient.pages.IonPages#fetchPage(String)} or {@link com.anfema.ionclient.pages.IonPages#fetchPagePreview(String)} instead.
	 */
	public static Func1<PagePreview, Boolean> identifierEquals( String pageIdentifier )
	{
		return pagePreview -> pagePreview != null && pagePreview.identifier != null && pagePreview.identifier.equals( pageIdentifier );
	}

	/**
	 * Fetch (preview) pages while providing a list of page identifiers.
	 */
	public static Func1<PagePreview, Boolean> identifierIn( List<String> pageIdentifiers )
	{
		return pagePreview -> {
			if ( pagePreview == null || pageIdentifiers == null )
			{
				return false;
			}

			for ( String pageIdentifier : pageIdentifiers )
			{
				if ( pageIdentifier != null && pageIdentifier.equals( pagePreview.identifier ) )
				{
					return true;
				}
			}
			return false;
		};
	}

	/**
	 * Fetch (preview) pages with a specific layout.
	 */
	public static Func1<PagePreview, Boolean> layoutEquals( String layout )
	{
		return pagePreview -> pagePreview != null && pagePreview.layout != null && pagePreview.layout.equals( layout );
	}

	/**
	 * Fetch (preview) pages with a specific direct tree parent.
	 */
	public static Func1<PagePreview, Boolean> childOf( String parentIdentifier )
	{
		return pagePreview -> pagePreview != null && pagePreview.parent != null && pagePreview.parent.equals( parentIdentifier );
	}
}
