package com.anfema.ampclient.utils;

import retrofit.HttpException;
import rx.Observable.Transformer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class RxUtils
{
	/**
	 * If an exception is thrown during a network call, it will be logged by default using this action.
	 */
	public static final Action1<Throwable> DEFAULT_EXCEPTION_HANDLER = exception -> {
		if ( exception instanceof HttpException )
		{
			HttpException httpException = ( HttpException ) exception;
			Log.e( "HTTP request failed and returned status " + httpException.code() + "." );
		}
		Log.ex( exception );
	};

	@SuppressWarnings("unchecked")
	public static <T> Transformer<T, T> applySchedulers()
	{
		return tObservable -> tObservable.subscribeOn( Schedulers.io() ).observeOn( AndroidSchedulers.mainThread() );
	}
}