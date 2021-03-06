package com.anfema.ionclient.mediafiles;

import android.net.Uri;
import android.widget.ImageView;

import com.anfema.ionclient.pages.ConfigUpdatable;
import com.anfema.ionclient.pages.models.contents.Downloadable;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import io.reactivex.Single;
import io.reactivex.functions.Function;

/**
 * Wraps load methods of {@link Picasso} to apply ION's caching mechanism. Also provides direct access to picasso instance via {@link #getPicassoInstance()}.
 */
public interface IonPicasso extends ConfigUpdatable
{
	/**
	 * {@link #loadImage(int, ImageView, Function, Callback)} without a callback
	 */
	void loadImage( int resourceId, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation );

	/**
	 * Load image from a resource ID.
	 */
	void loadImage( int resourceId, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation, Callback callback );

	/**
	 * {@link #loadImage(String, ImageView, Function, Callback)} without a callback
	 */
	void loadImage( String path, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation );

	/**
	 * Convenience method for {@link #loadImage(Downloadable, ImageView, Function, Callback)} which does not provide a {@link Downloadable} image
	 * object but only a path without checksum.
	 */
	void loadImage( String path, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation, Callback callback );

	/**
	 * {@link #loadImage(Downloadable, ImageView, Function, Callback)} without a callback
	 */
	void loadImage( Downloadable image, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation );

	/**
	 * Load image form a URI. Wraps Picasso's load method {@link Picasso#load(Uri)}.
	 * If URI is a HTTP link then it might be changed to a file link in case the cached version shall be used.
	 *
	 * @param image                 image content. the URL can be a network link, file link, or content link, see {@link Picasso#load(Uri)}
	 * @param target                view where the image should be displayed
	 * @param requestTransformation can be null, chained operations on {@link RequestCreator} (e.g. requestCreator -> requestCreator.fit().centerCrop())
	 * @param callback              listener to onSuccess and onError events
	 */
	void loadImage( Downloadable image, ImageView target, Function<RequestCreator, RequestCreator> requestTransformation, Callback callback );

	/**
	 * loadImage methods are a shortcut for {@link Picasso}'s load methods. Get Picasso instance if you need more of its functions.
	 * <p>
	 * Warning: If authentication value is not provided directly, but through an async function, this method will return an un-initialized instance.
	 * The safe option is {@link #getPicassoInstanceDoAuthCall()}.
	 */
	Picasso getPicassoInstance();

	/**
	 * @see #getPicassoInstanceDoAuthCall()
	 * <p>
	 * If authentication value is not provided directly, but through an async function, this method will return a guaranteed initialized instance.
	 */
	Single<Picasso> getPicassoInstanceDoAuthCall();
}
