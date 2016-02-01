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
	 * Wraps Picasso's load method {@link Picasso#load(Uri)}.
	 * If Uri is a HTTP link then it might be changed to a file link in case the cached version shall be used.
	 *
	 * @param uri                   the location of the image source. can be a network link, file link, or content link, see {@link Picasso#load(Uri)}
	 * @param target                view where the image should be displayed
	 * @param requestTransformation can be null, chained operations on {@link RequestCreator} (e.g. requestCreator -> requestCreator.fit().centerCrop())
	 */
	void loadImage( Uri uri, ImageView target, Func1<RequestCreator, RequestCreator> requestTransformation );

	/**
	 * Convenience method for {@link #loadImage(Uri, ImageView, Func1)} which parses path into {@link Uri}
	 */
	void loadImage( String path, ImageView target, Func1<RequestCreator, RequestCreator> requestTransformation );

	/**
	 * Wraps Picasso's load method {@link Picasso#load(int)}.
	 * The only reason it is wrapped is that the same Picasso instance and, thus, the same memory cache is used.
	 * It is equivalent to calling {@link #getPicassoInstance()} followed by {@link Picasso#load(int)} and apply the operators on {@link RequestCreator}.
	 */
	void loadImage( int resourceId, ImageView target, Func1<RequestCreator, RequestCreator> requestTransformation );

	/**
	 * loadImage methods are a shortcut for {@link Picasso}'s load methods. Get Picasso instance if you need more of its functions.
	 */
	Picasso getPicassoInstance();
}
