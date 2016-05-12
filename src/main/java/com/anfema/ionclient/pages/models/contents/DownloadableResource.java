package com.anfema.ionclient.pages.models.contents;

/**
 * The most basic implementation of {@link Downloadable} interface. Use to construct a Downloadable from a URL string.
 */
public class DownloadableResource implements Downloadable
{
	private final String url;

	public DownloadableResource( String url )
	{
		this.url = url;
	}

	@Override
	public String getUrl()
	{
		return url;
	}

	@Override
	public String getChecksum()
	{
		return null;
	}
}
