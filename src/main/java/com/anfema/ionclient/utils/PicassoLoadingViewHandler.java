package com.anfema.ionclient.utils;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Callback;

import io.reactivex.functions.Function;

/**
 * Imagine you do not want to use Picasso's placeholder during loading but a progress bar for example.
 * <p>
 * First create a (relative) layout with that extra view indicating loading state.
 * Then, pick {@link com.anfema.ionclient.mediafiles.IonPicasso#loadImage(Uri, ImageView, Function, Callback)} or a convenience method with a callback
 * and pass an instance of {@link PicassoLoadingViewHandler} pointing to the loading indicator and visibility will be handled automatically.
 */
public class PicassoLoadingViewHandler implements Callback
{
	private final View loadingIndicatorView;

	public PicassoLoadingViewHandler( View loadingIndicatorView )
	{
		this.loadingIndicatorView = loadingIndicatorView;
		if ( this.loadingIndicatorView != null )
		{
			this.loadingIndicatorView.setVisibility( View.VISIBLE );
		}
	}

	@Override
	public void onSuccess()
	{
		if ( loadingIndicatorView != null )
		{
			loadingIndicatorView.setVisibility( View.GONE );
		}
	}

	@Override
	public void onError()
	{
		if ( loadingIndicatorView != null )
		{
			loadingIndicatorView.setVisibility( View.GONE );
		}
	}
}
