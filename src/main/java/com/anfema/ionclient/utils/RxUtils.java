package com.anfema.ionclient.utils;


import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class RxUtils
{
	/**
	 * If an exception is thrown during a network call, it will be logged by default using this action.
	 */
	public static final Consumer<Throwable> DEFAULT_EXCEPTION_HANDLER = RxUtils::logException;

	public static void logException( Throwable exception )
	{
		if ( exception instanceof HttpException )
		{
			HttpException httpException = ( HttpException ) exception;
			IonLog.e( "HTTP request failed and returned status " + httpException.code() + "." );
		}
		IonLog.ex( exception );
	}

	public static <A, B, C> Observable<C> flatZip( Observable<A> o1, Observable<B> o2, BiFunction<A, B, Observable<C>> func )
	{
		Observable<Observable<C>> obob = Observable.zip( o1, o2, func );
		// flatten observable in observable
		return obob.flatMap( x -> x );
	}

	public static <A, B, C> Observable<C> flatCombineLatest( Observable<A> o1, Observable<B> o2, BiFunction<A, B, Observable<C>> func )
	{
		Observable<Observable<C>> obob = Observable.combineLatest( o1, o2, func );
		// flatten observable in observable
		return obob.flatMap( x -> x );
	}
}
