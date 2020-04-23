package com.anfema.ionclient.mediafiles;

import android.net.Uri;
import androidx.annotation.NonNull;

public class UriWithStatus
{
	public final Uri        uri;
	public final FileStatus status;

	public UriWithStatus( Uri uri, FileStatus status )
	{
		this.uri = uri;
		this.status = status;
	}

	public UriWithStatus( @NonNull FileWithStatus fileWithStatus )
	{
		this.uri = Uri.fromFile( fileWithStatus.file );
		this.status = fileWithStatus.status;
	}
}

