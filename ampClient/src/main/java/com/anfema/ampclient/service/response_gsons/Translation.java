package com.anfema.ampclient.service.response_gsons;

public class Translation
{
	private Content[] content;

	private String locale;

	public Content[] getContent()
	{
		return content;
	}

	public void setContent( Content[] content )
	{
		this.content = content;
	}

	public String getLocale()
	{
		return locale;
	}

	public void setLocale( String locale )
	{
		this.locale = locale;
	}

	@Override
	public String toString()
	{
		return "ClassPojo [content = " + content + ", locale = " + locale + "]";
	}
}
