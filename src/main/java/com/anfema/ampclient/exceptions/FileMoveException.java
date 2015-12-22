package com.anfema.ampclient.exceptions;

import java.io.File;

public class FileMoveException extends IllegalArgumentException
{
	public FileMoveException( File source, File target )
	{
		super( "Source " + source.getPath() + " is a " + ( source.isDirectory() ? "directory" : "file" ) + ( source.exists() ? "end exists" : "" )
				+ ". Target " + target.getPath() + " is a " + ( target.isDirectory() ? "directory" : "file" ) + ( target.exists() ? "end exists" : "" ) + "." );
	}
}
