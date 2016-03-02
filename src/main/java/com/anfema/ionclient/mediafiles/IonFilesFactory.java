package com.anfema.ionclient.mediafiles;

import android.content.Context;

import com.anfema.ionclient.IonConfig;

public class IonFilesFactory
{
	public static IonFiles newInstance( IonConfig config, Context context )
	{
		return new IonFilesWithCaching( config, context );
	}
}
