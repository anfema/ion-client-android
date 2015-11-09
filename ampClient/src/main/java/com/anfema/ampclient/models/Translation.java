package com.anfema.ampclient.models;

import com.anfema.ampclient.models.contents.AContent;

import java.util.ArrayList;

public class Translation
{
	public String locale;

	public ArrayList<AContent> content;

	@Override
	public String toString()
	{
		return "Translation [content = " + content + ", locale = " + locale + "]";
	}
}
