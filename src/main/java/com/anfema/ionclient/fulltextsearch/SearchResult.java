package com.anfema.ionclient.fulltextsearch;

public class SearchResult
{
	public String pageLayout;
	public String pageIdentifier;
	public String text;

	public SearchResult( String pageIdentifier, String text, String pageLayout )
	{
		this.pageIdentifier = pageIdentifier;
		this.text = text;
		this.pageLayout = pageLayout;
	}
}
