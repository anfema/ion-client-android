package com.anfema.ampclient.service.models;

public abstract class AContent
{
	// general

	private String type;

	private String variation;

	private String outlet;

	private String position; // not used by container outlet

	private String is_searchable; // not used by container outlet

	public String getType()
	{
		return type;
	}

	public void setType( String type )
	{
		this.type = type;
	}

	public String getVariation()
	{
		return variation;
	}

	public void setVariation( String variation )
	{
		this.variation = variation;
	}

	public String getOutlet()
	{
		return outlet;
	}

	public void setOutlet( String outlet )
	{
		this.outlet = outlet;
	}

	@Override
	public String toString()
	{
		return "Content [type = " + type + ", variation = " + variation + ", outlet = " + outlet + ", type = " + type + " position = "
				+ position + ", outlet = " + outlet + ", is_searchable = " + is_searchable + ", variation = " + variation + "]";
	}
}