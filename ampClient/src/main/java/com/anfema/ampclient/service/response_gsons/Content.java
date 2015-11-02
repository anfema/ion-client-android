package com.anfema.ampclient.service.response_gsons;

public class Content
{
	// general

	private String type;

	private String variation;

	private String position;

	private String outlet;

	private String is_searchable;

	// container content

	private Content[] children;

	// color content

	private String r;

	private String g;

	private String b;

	private String a;

	// image content

	private String original_checksum;

	private String scale;

	private String original_image;

	private String file_size;

	private String width;

	private String image;

	private String checksum;

	private String original_file_size;

	private String original_width;

	private String original_mime_type;

	private String translation_x;

	private String translation_y;

	private String height;

	private String mime_type;

	private String original_height;





	public String getOutlet()
	{
		return outlet;
	}

	public void setOutlet( String outlet )
	{
		this.outlet = outlet;
	}

	public String getVariation()
	{
		return variation;
	}

	public void setVariation( String variation )
	{
		this.variation = variation;
	}

	public Content[] getChildren()
	{
		return children;
	}

	public void setChildren( Content[] children )
	{
		this.children = children;
	}

	public String getType()
	{
		return type;
	}

	public void setType( String type )
	{
		this.type = type;
	}

	@Override
	public String toString()
	{
		return "ClassPojo [outlet = " + outlet + ", variation = " + variation + ", children = " + children + ", type = " + type + " position = "
				+ position + ",g = " + g + ", outlet = " + outlet + ", is_searchable = " + is_searchable
				+ ", b = " + b + ", r = " + r + ", a = " + a + ", variation = " + variation + ", type = " + type + "]";
	}
}