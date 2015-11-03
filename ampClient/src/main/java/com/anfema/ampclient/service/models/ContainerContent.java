package com.anfema.ampclient.service.models;

import java.util.ArrayList;

public class ContainerContent extends AContent
{
	private ArrayList<AContent> children;

	public ArrayList<AContent> getChildren()
	{
		return children;
	}

	public void setChildren( ArrayList<AContent> children )
	{
		this.children = children;
	}
}
