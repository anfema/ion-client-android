package com.anfema.ampclient.pages.models.responses;

import com.anfema.ampclient.pages.models.Page;

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
