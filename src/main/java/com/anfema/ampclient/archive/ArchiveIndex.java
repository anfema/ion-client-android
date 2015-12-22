package com.anfema.ampclient.archive;

import android.support.annotation.NonNull;

import java.util.List;

public class ArchiveIndex
{
	public String name;

	public String url;

	public String checksum;

	public static ArchiveIndex getByName( @NonNull String name, @NonNull List<ArchiveIndex> indexList )
	{
		for ( ArchiveIndex archiveIndex : indexList )
		{
			if ( name.equals( archiveIndex.name ) )
			{
				return archiveIndex;
			}
		}
		return null;
	}
}
