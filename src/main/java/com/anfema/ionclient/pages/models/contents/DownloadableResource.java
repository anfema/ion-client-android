package com.anfema.ionclient.pages.models.contents;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * The most basic implementation of {@link Downloadable} interface. Use to construct a Downloadable from a URL string.
 */
public class DownloadableResource implements Downloadable
{
	@NonNull
	private final String url;
	@Nullable
	private final String checksum;

	public DownloadableResource( @NonNull String url, @Nullable String checksum )
	{
		this.url = url;
		this.checksum = checksum;
	}

	public DownloadableResource( @NonNull String url )
	{
		this( url, null );
	}

	@NonNull
	@Override
	public String getUrl()
	{
		return url;
	}

	@Nullable
	@Override
	public String getChecksum()
	{
		return checksum;
	}
}
