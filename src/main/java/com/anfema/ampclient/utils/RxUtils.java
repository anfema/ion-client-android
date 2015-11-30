package com.anfema.ampclient.utils;

import retrofit.HttpException;
import rx.Observable.Transformer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class RxUtils
{
	/**
	 * If an exception is thrown during a network call, it will be logged by default using this action.
	 */
	public static final Action1<Throwable> DEFAULT_EXCEPTION_HANDLER = RxUtils::logException;

	public static void logException( Throwable exception )
	{
		if ( exception instanceof HttpException )
		{
			HttpException httpException = ( HttpException ) exception;
			Log.e( "HTTP request failed and returned status " + httpException.code() + "." );
		}
		Log.ex( exception );
	}

	/**
	 * Convenience action for on error:
	 * This is an action that receives Throwable but ignores exception. Use only if exception is handled at another spot.
	 */
	public static Action1<Throwable> IGNORE_ERROR = throwable -> {
	};

	/**
	 * Convenience action for on completed:
	 * This is an action that receives no arguments and does nothing.
	 */
	public static Action0 NOTHING = () -> {
	};


	/**
	 * Use in compose operator to apply to every observable in the chain
	 */
	@SuppressWarnings("unchecked")
	public static <T> Transformer<T, T> applySchedulers()
	{
		return tObservable -> tObservable.subscribeOn( Schedulers.io() ).observeOn( AndroidSchedulers.mainThread() );
	}
}