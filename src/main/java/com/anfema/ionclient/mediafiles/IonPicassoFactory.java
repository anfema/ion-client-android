package com.anfema.ionclient.mediafiles;

import android.content.Context;

import com.anfema.ionclient.IonConfig;

public class IonPicassoFactory
{
	public static IonPicasso newInstance( IonFiles ionFiles, IonConfig config, Context context )
	{
		return new IonPicassoWithCaching( ionFiles, config, context );
	}
}
