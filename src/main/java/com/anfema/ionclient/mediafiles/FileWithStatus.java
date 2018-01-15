package com.anfema.ionclient.mediafiles;

import java.io.File;

public class FileWithStatus
{
	public final File       file;
	public final FileStatus status;

	public FileWithStatus( File file, FileStatus status )
	{
		this.file = file;
		this.status = status;
	}
}
