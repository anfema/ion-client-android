package com.anfema.ionclient.pages.models.contents;

public class ImageContent extends Content implements Downloadable
{
	public String mime_type;

	public String image;

	public int width;

	public int height;

	public int file_size;

	public String checksum;


	public String original_mime_type;

	public String original_image;

	public int original_width;

	public int original_height;

	public int original_file_size;

	public String original_checksum;


	public int translation_x;

	public int translation_y;

	public float scale;

	@Override
	public String getUrl()
	{
		return image;
	}

	@Override
	public String getChecksum()
	{
		return checksum;
	}
}
