package com.anfema.ionclient.pages.models.contents;

@SuppressWarnings("unused")
public class FileContent extends Content implements Downloadable
{
	public String file_size;

	public String mime_type;

	public String file;

	public String name;

	public String checksum;

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