package com.anfema.ampclient.service.models;

import java.util.ArrayList;

public class Translation
{
	private String locale;

	private ArrayList<AContent> content;

	public String getLocale()
	{
		return locale;
	}

	public void setLocale( String locale )
	{
		this.locale = locale;
	}

	public ArrayList<AContent> getContent()
	{
		return content;
	}

	public void setContent( ArrayList<AContent> content )
	{
		this.content = content;
	}

	@Override
	public String toString()
	{
		return "Translation [content = " + content + ", locale = " + locale + "]";
	}
}
