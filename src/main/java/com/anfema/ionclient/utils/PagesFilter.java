package com.anfema.ionclient.utils;

import com.anfema.ionclient.pages.models.PagePreview;

import java.util.List;

import rx.functions.Func1;

public class PagesFilter
{

	public static Func1<PagePreview, Boolean> identifierEquals( String pageIdentifier )
	{
		return pagePreview -> pagePreview.identifier != null && pagePreview.identifier.equals( pageIdentifier );
	}

	public static Func1<PagePreview, Boolean> identifierIn( List<String> pageIdentifiers )
	{
		return pagePreview -> {
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
