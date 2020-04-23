package com.anfema.utils;

import androidx.annotation.NonNull;

public class ExceptionUtils
{
	@NonNull
	public static Exception fromThrowable( Throwable throwable )
	{
		Exception e;
		if ( throwable instanceof Exception )
		{
			e = ( Exception ) throwable;
		}
		else
		{
			e = new Exception( throwable );
		}
		return e;
	}
}
