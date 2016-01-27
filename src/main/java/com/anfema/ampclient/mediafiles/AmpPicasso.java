package com.anfema.ampclient.mediafiles;

import android.net.Uri;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

public interface AmpPicasso
{
	RequestCreator loadImage( String path );

	RequestCreator loadImage( Uri uri );

	RequestCreator loadImage( int resourceID );

	/**
	 * loadImage methods are a shortcut for {@link Picasso}'s load methods. Get Picasso instance if you need more of its functions.
	 */
	Picasso getPicassoInstance();
}
