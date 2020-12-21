package com.anfema.ionclient.exceptions;

import java.io.File;

public class FileMoveException extends IllegalArgumentException
{
	public FileMoveException( File source, File target )
	{
		super(
				"Source " + source.getPath() + " is a " + ( source.isDirectory() ? "directory" : "file" )
				+ ", which does " + (( source.exists() ? "" : "NOT") + " exist.\n" )
				+ "Target " + target.getPath() + " is a " + ( target.isDirectory() ? "directory" : "file" )
				+ ", which does " + ( (target.exists() ? "" : "NOT") + " exist." )
		);
	}
}
