package com.anfema.ampclient.mediafiles;

import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import rx.functions.Func1;

/**
 * Wraps load methods of {@link Picasso} to apply AMP's caching mechanism. Also provides direct access to picasso instance via {@link #getPicassoInstance()}.
 */
public interface AmpPicasso
{
	/**
	 * Wraps Picassos load method {@link Picasso#load(Uri)}.
	 * If Uri is a HTTP link then it might be changed to a file link in case the cached version shall be used.
	 */
	void loadImage( Uri uri, ImageView target, Func1<RequestCreator, RequestCreator> requestTransformation );

	/**
	 * Convenience method for {@link #loadImage(Uri, ImageView, Func1)} which parses path into {@link Uri}
	 * Wraps Picassos load method {@link Picasso#load(String)}
	 */
	void loadImage( String path, ImageView target, Func1<RequestCreator, RequestCreator> requestTransformation );

	/**
	 * loadImage methods are a shortcut for {@link Picasso}'s load methods. Get Picasso instance if you need more of its functions.
	 */
	Picasso getPicassoInstance();
}
