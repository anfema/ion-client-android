package com.anfema.ionclient.utils;

import com.anfema.ionclient.pages.models.PagePreview;

import java.util.List;

import rx.functions.Func1;

/**
 * This utility filters are supposed to be used with
 * {@link com.anfema.ionclient.pages.IonPages#getPagePreviews(Func1)},
 * {@link com.anfema.ionclient.pages.IonPages#getPagePreviewsSorted(Func1)},
 * {@link com.anfema.ionclient.pages.IonPages#getPages(Func1)}, and
 * {@link com.anfema.ionclient.pages.IonPages#getPagesSorted(Func1)}
 */
public class PagesFilter
{
	/**
	 * Before using this filter method, consider using
	 * {@link com.anfema.ionclient.pages.IonPages#getPage(String)} or {@link com.anfema.ionclient.pages.IonPages#getPagePreview(String)} instead.
	 */
	public static Func1<PagePreview, Boolean> identifierEquals( String pageIdentifier )
	{
		return pagePreview -> pagePreview.identifier != null && pagePreview.identifier.equals( pageIdentifier );
	}

	public static Func1<PagePreview, Boolean> identifierIn( List<String> pageIdentifiers )
	{
		return pagePreview -> {
			if ( pageIdentifiers == null )
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

	public static Func1<PagePreview, Boolean> titleEquals( String title )
	{
		return pagePreview -> pagePreview.metaEquals( "title", title );
	}

	public static Func1<PagePreview, Boolean> layoutEquals( String layout )
	{
		return pagePreview -> pagePreview.layout != null && pagePreview.layout.equals( layout );
	}

	public static Func1<PagePreview, Boolean> childOf( String parentIdentifier )
	{
		return pagePreview -> pagePreview.parent != null && pagePreview.parent.equals( parentIdentifier );
	}
}
