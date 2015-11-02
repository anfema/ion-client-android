package com.anfema.ampclient.service.response_gsons;

public class PagesResponse
{
	private Page[] page;

	private Meta meta;

	public Page[] getPages()
	{
		return page;
	}

	public void setPage( Page[] page )
	{
		this.page = page;
	}

	public Meta getMeta()
	{
		return meta;
	}

	public void setMeta( Meta meta )
	{
		this.meta = meta;
	}

	@Override
	public String toString()
	{
		return "ClassPojo [page = " + page + ", meta = " + meta + "]";
	}
}
