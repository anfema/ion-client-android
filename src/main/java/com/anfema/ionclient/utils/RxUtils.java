package com.anfema.ionclient.utils;

import retrofit2.HttpException;
import rx.Observable;
import rx.Observable.Transformer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
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
	 * Convenience action for onNext function.
	 * This is an action that receives one argument and does nothing.
	 */
	public static Action1<Object> NOTHING = o -> doNothing();

	public static void doNothing()
	{
		// does what you would expect
	}

	/**
	 * Use in compose operator to apply to every observable in the chain
	 */
	@SuppressWarnings("unchecked")
	public static <T> Transformer<T, T> runOnIoThread()
	{
		return tObservable -> tObservable.subscribeOn( Schedulers.io() ).observeOn( AndroidSchedulers.mainThread() );
	}

	/**
	 * Use in compose operator to apply to every observable in the chain
	 */
	@SuppressWarnings("unchecked")
	public static <T> Transformer<T, T> runOnComputionThread()
	{
		return tObservable -> tObservable.subscribeOn( Schedulers.computation() ).observeOn( AndroidSchedulers.mainThread() );
	}

	public static <A, B, C> Observable<C> flatZip( Observable<A> o1, Observable<B> o2, Func2<A, B, Observable<C>> func )
	{
		Observable<Observable<C>> obob = Observable.zip( o1, o2, func::call );
		// flatten observable in observable
		return obob.flatMap( x -> x );
	}

	public static <A, B, C> Observable<C> flatCombineLatest( Observable<A> o1, Observable<B> o2, Func2<A, B, Observable<C>> func )
	{
		Observable<Observable<C>> obob = Observable.combineLatest( o1, o2, func::call );
		// flatten observable in observable
		return obob.flatMap( x -> x );
	}
}