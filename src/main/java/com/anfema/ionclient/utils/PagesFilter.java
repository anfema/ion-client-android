package com.anfema.ionclient.utils;

import android.support.annotation.NonNull;

import com.anfema.ionclient.pages.models.PagePreview;

import java.util.List;

import io.reactivex.functions.Predicate;

/**
 * This utility filters are supposed to be used with {@link com.anfema.ionclient.pages.IonPages#fetchPagePreviews(Predicate)}
 * and {@link com.anfema.ionclient.pages.IonPages#fetchPages(Predicate)}.
 */
public class PagesFilter
{
	/**
	 * Fetch all (preview) pages of current collection.
	 */
	public static final Predicate<PagePreview> ALL = pagePreview -> pagePreview != null;

	/**
	 * Fetch all (preview) pages, which are (tree-hierarchical) root pages within the collection.
	 */
	public static final Predicate<PagePreview> ROOT_ELEMENTS = pagePreview -> pagePreview != null && pagePreview.parent == null;

	/**
	 * Before using this filter method, consider using
	 * {@link com.anfema.ionclient.pages.IonPages#fetchPage(String)} or {@link com.anfema.ionclient.pages.IonPages#fetchPagePreview(String)} instead.
	 */
	public static Predicate<PagePreview> identifierEquals( String pageIdentifier )
	{
		return pagePreview -> identifierEquals( pageIdentifier, pagePreview );
	}

	public static boolean identifierEquals( String pageIdentifier, PagePreview pagePreview )
	{
		return pagePreview != null && pagePreview.identifier != null && pagePreview.identifier.equals( pageIdentifier );
	}

	/**
	 * Fetch (preview) pages while providing a list of page identifiers.
	 */
	public static Predicate<PagePreview> identifierIn( List<String> pageIdentifiers )
	{
		return pagePreview -> identifierIn( pageIdentifiers, pagePreview );
	}

	@NonNull
	public static Boolean identifierIn( List<String> pageIdentifiers, PagePreview pagePreview )
	{
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
	}

	/**
	 * Fetch (preview) pages with a specific layout.
	 */
	public static Predicate<PagePreview> layoutEquals( String layout )
	{
		return pagePreview -> layoutEquals( layout, pagePreview );
	}

	public static boolean layoutEquals( String layout, PagePreview pagePreview )
	{
		return pagePreview != null && pagePreview.layout != null && pagePreview.layout.equals( layout );
	}

	/**
	 * Fetch (preview) pages with a specific direct tree parent.
	 */
	public static Predicate<PagePreview> childOf( String parentIdentifier )
	{
		return pagePreview -> childOf( parentIdentifier, pagePreview );
	}

	public static boolean childOf( String parentIdentifier, PagePreview pagePreview )
	{
		return pagePreview != null && pagePreview.parent != null && pagePreview.parent.equals( parentIdentifier );
	}
}
