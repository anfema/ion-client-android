package com.anfema.ionclient.pages.models.contents;

@SuppressWarnings("unused")
public class MediaContent extends Content implements Downloadable
{
	public String original_checksum;

	public String file_size;

	public String width;

	public String checksum;

	public String original_file_size;

	public String original_width;

	public String original_mime_type;

	public String height;

	public String mime_type;

	public String original_file;

	public String file;

	public String name;

	public String length;

	public String original_height;

	public String original_length;

	@Override
	public String getUrl()
	{
		return file;
	}

	@Override
	public String getChecksum()
	{
		return checksum;
	}
}
