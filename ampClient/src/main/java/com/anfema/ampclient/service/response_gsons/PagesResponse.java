package com.anfema.ampclient.service.response_gsons;

public class PagesResponse
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
