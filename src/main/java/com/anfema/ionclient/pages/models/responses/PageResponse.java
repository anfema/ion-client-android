package com.anfema.ionclient.pages.models.responses;

import com.anfema.ionclient.pages.models.Page;

public class PageResponse
{
	private Page[] page;

	public Page getPage()
	{
		return page[ 0 ];
	}

	@Override
	public String toString()
	{
		return "PagesResponse [page = " + getPage() + "]";
	}
}
