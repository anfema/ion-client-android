package com.anfema.ionclient;

import android.content.Context;

import com.anfema.ionclient.exceptions.IonConfigInvalidException;
import com.anfema.ionclient.utils.IonLog;
import com.anfema.ionclient.utils.MemoryUtils;
import com.anfema.ionclient.utils.PendingDownloadHandler;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

public class IonConfig
{
	public static final String DEFAULT_VARIATION                        = "default";
	public static final int    DEFAULT_MINUTES_UNTIL_COLLECTION_REFETCH = 5;

	/**
	 * Defines strategies, when to fetch data from cache and when to download it from internet.
	 */
	public enum CachingStrategy
	{
		/**
		 * strategy:
		 * 1. fetch current version from cache
		 * 2. download current version (if connected to internet)
		 * 3. fetch possibly outdated version from cache (if it exists)
		 * 4. error (because no version in cache exists and no internet connection)
		 */
		NORMAL,
		/**
		 * strategy:
		 * 1. fetch (possibly outdated) version from cache (if it exists)
		 * 2. error (because no version in cache exists and downloading is prohibited with this mode)
		 */
		STRICT_OFFLINE
	}

	// *** global configuration ***

	/**
	 * @see CachingStrategy
	 */
	public static CachingStrategy cachingStrategy = CachingStrategy.NORMAL;

	/**
	 * How many pages are kept in LRU memory cache? (for all client instances accumulated), unit: no. of page entries
	 * <p>
	 * Value must be overwritten before first ION request is made, otherwise it won't have any effect.
	 * It is recommended to set it as early as possible, e.g. in onCreate() of application (or first activity).
	 * <p>
	 * Must be set to a positive value.
	 */
	public static int pagesMemCacheSize = 100;

	/**
	 * Maximum size of memory cache, which all picasso instances share. The Unit is bytes.
	 * <p>
	 * Value must be overwritten before first ION request is made, otherwise it won't have any effect.
	 * It is recommended to set it as early as possible, e.g. in onCreate() of application (or first activity).
	 * <p>
	 * Be careful not to exceed the available RAM of the application.
	 * You might want to calculate the memory cache size using {@link MemoryUtils#calculateAvailableMemCache(Context)}.
	 * <p>
	 * If not set to a positive value, default cache size will be used.
	 */
	public static int picassoMemCacheSize = -1;

	public static int logLevel = IonLog.NONE;


	private static final Map<IonConfig, String>                    authorizations = new HashMap<>();
	private static final PendingDownloadHandler<IonConfig, String> pendingLogins  = new PendingDownloadHandler<>();


	// *** configuration of client instance ***

	/**
	 * base URL pointing to the ION endpoint
	 */
	public final String baseUrl;

	/**
	 * the collection identifier, {@link IonClient} will use for its calls
	 */
	public final String collectionIdentifier;

	/**
	 * Which language shall be requested? (e.g. "de_DE")
	 */
	public final String locale;

	/**
	 * For which platform/resolution are pages requested?
	 */
	public final String variation;

	/**
	 * Authorization header value is required to use the ION API. Primary option: provide it directly by passing the value.
	 */
	private String authorizationHeaderValue;

	/**
	 * Authorization header value is required to use the ION API. Secondary option: provide it indirectly through an async call.
	 */
	private final Observable<String> authorizationHeaderValueCall;

	/**
	 * Should the whole archive be downloaded when the collection is downloaded?
	 */
	public final boolean archiveDownloads;

	/**
	 * Should the full text search database be downloaded when the collection is downloaded?
	 */
	public final boolean ftsDbDownloads;

	/**
	 * Time after which collection is refreshed = fetched from server again.
	 */
	public final int minutesUntilCollectionRefetch;

	@SuppressWarnings("unused")
	public static class Builder
	{
		private final String baseUrl;
		private final String collectionIdentifier;
		private       String locale;
		private String             variation                     = DEFAULT_VARIATION;
		private String             authorizationHeaderValue      = null;
		private Observable<String> authorizationHeaderValueCall  = null;
		private boolean            archiveDownloads              = false;
		private boolean            ftsDbDownloads                = false;
		private int                minutesUntilCollectionRefetch = DEFAULT_MINUTES_UNTIL_COLLECTION_REFETCH;

		public Builder( String baseUrl, String collectionIdentifier )
		{
			this.baseUrl = baseUrl;
			this.collectionIdentifier = collectionIdentifier;
		}

		public Builder locale( String locale )
		{
			this.locale = locale;
			return this;
		}

		/**
		 * Set locale from device configuration
		 */
		public Builder locale( Context context )
		{
			this.locale = context.getResources().getConfiguration().locale.toString();
			return this;
		}

		public Builder variation( String variation )
		{
			this.variation = variation;
			return this;
		}

		public Builder authorization( String authorizationHeaderValue )
		{
			this.authorizationHeaderValue = authorizationHeaderValue;
			return this;
		}

		public Builder authorization( Observable<String> authorizationHeaderValueCall )
		{
			this.authorizationHeaderValueCall = authorizationHeaderValueCall;
			return this;
		}

		public Builder archiveDownloads( boolean archiveDownloads )
		{
			this.archiveDownloads = archiveDownloads;
			return this;
		}

		public Builder ftsDbDownloads( boolean ftsDbDownloads )
		{
			this.ftsDbDownloads = ftsDbDownloads;
			return this;
		}

		public Builder minutesUntilCollectionRefetch( int minutesUntilCollectionRefetch )
		{
			this.minutesUntilCollectionRefetch = minutesUntilCollectionRefetch;
			return this;
		}

		public IonConfig build()
		{
			if ( locale == null )
			{
				throw new IllegalStateException( "locale == null" );
			}
			if ( authorizationHeaderValue == null && authorizationHeaderValueCall == null )
			{
				IonLog.w( "IonConfig.Builder", "Did you forget to provide an authorization?" );
			}
			return new IonConfig( baseUrl, collectionIdentifier, locale, variation, authorizationHeaderValue, authorizationHeaderValueCall,
					archiveDownloads, ftsDbDownloads, minutesUntilCollectionRefetch );
		}
	}

	public IonConfig( String baseUrl, String collectionIdentifier, String locale, String variation, String authorizationHeaderValue, Observable<String> authorizationHeaderValueCall, boolean archiveDownloads, boolean ftsDbDownloads, int minutesUntilCollectionRefetch )
	{
		this.baseUrl = baseUrl;
		this.collectionIdentifier = collectionIdentifier;
		this.locale = locale;
		this.variation = variation;
		this.authorizationHeaderValue = authorizationHeaderValue;
		this.authorizationHeaderValueCall = authorizationHeaderValueCall;
		this.archiveDownloads = archiveDownloads;
		this.ftsDbDownloads = ftsDbDownloads;
		this.minutesUntilCollectionRefetch = minutesUntilCollectionRefetch;
	}

	public IonConfig( IonConfig otherConfig )
	{
		this.baseUrl = otherConfig.baseUrl;
		this.collectionIdentifier = otherConfig.collectionIdentifier;
		this.locale = otherConfig.locale;
		this.variation = otherConfig.variation;
		this.authorizationHeaderValue = otherConfig.authorizationHeaderValue;
		this.authorizationHeaderValueCall = otherConfig.authorizationHeaderValueCall;
		this.archiveDownloads = otherConfig.archiveDownloads;
		this.ftsDbDownloads = otherConfig.ftsDbDownloads;
		this.minutesUntilCollectionRefetch = otherConfig.minutesUntilCollectionRefetch;
	}

	public boolean isValid()
	{
		return baseUrl != null && baseUrl.contains( "://" )
				&& collectionIdentifier != null
				&& locale != null && locale.length() > 0
				&& minutesUntilCollectionRefetch >= 0
				&& pagesMemCacheSize >= 0;
	}

	public static void assertConfigIsValid( IonConfig config )
	{
		if ( config == null || !config.isValid() )
		{
			throw new IonConfigInvalidException();
		}
	}

	/**
	 * To check that attributes are equal which are ESSENTIAL for the IDENTITY of ION client
	 */
	@Override
	public boolean equals( Object obj )
	{
		if ( obj == this )
		{
			return true;
		}
		if ( !( obj instanceof IonConfig ) )
		{
			return false;
		}

		IonConfig other = ( IonConfig ) obj;
		return other.baseUrl.equals( baseUrl )
				&& other.collectionIdentifier.equals( collectionIdentifier )
				&& other.locale.equals( locale )
				&& other.variation.equals( variation );
	}

	/**
	 * To check that EVERY attribute is equal
	 */
	public boolean strictEquals( Object obj )
	{
		if ( !equals( obj ) )
		{
			return false;
		}
		IonConfig other = ( IonConfig ) obj;
		return other.authorizationHeaderValue.equals( authorizationHeaderValue )
				&& other.authorizationHeaderValueCall.equals( authorizationHeaderValueCall )
				&& other.archiveDownloads == archiveDownloads
				&& other.ftsDbDownloads == ftsDbDownloads
				&& other.minutesUntilCollectionRefetch == minutesUntilCollectionRefetch;
	}

	@Override
	public int hashCode()
	{
		Object[] hashRelevantFields = { baseUrl, collectionIdentifier, locale, variation };
		return Arrays.hashCode( hashRelevantFields );
	}

	/**
	 * @param requestFunc the in parameter is the authorization header value
	 * @param <T>         return type of the request
	 * @return observable of request is forwarded
	 */
	public <T> Observable<T> authenticatedRequest( Func0<Observable<T>> requestFunc )
	{
		return authenticatedRequest( authorizationHeaderValue -> requestFunc.call() );
	}

	/**
	 * @param requestFunc the in parameter is the authorization header value
	 * @param <T>         return type of the request
	 * @return observable of request is forwarded
	 */
	public <T> Observable<T> authenticatedRequest( Func1<String, Observable<T>> requestFunc )
	{
		return authenticatedRequest( requestFunc, true );
	}

	private <T> Observable<T> authenticatedRequest( Func1<String, Observable<T>> requestFunc, boolean tryAgain )
	{
		return updateAuthorizationHeaderValue( !tryAgain )
				.flatMap( requestFunc::call )
				.flatMap( response -> {
					if ( tryAgain )
					{
						int responseCode = -1;
						if ( response instanceof Response )
						{
							responseCode = ( ( Response ) response ).code();
						}
						else if ( response instanceof retrofit2.Response )
						{
							responseCode = ( ( retrofit2.Response ) response ).code();
						}
						else
						{
							IonLog.w( "IonConfig", "authenticatedRequest: could not determine if request was unauthorized." );
						}

						if ( responseCode == HttpURLConnection.HTTP_UNAUTHORIZED )
						{
							return authenticatedRequest( requestFunc, false );
						}
					}
					return Observable.just( response );
				} );
	}

	public Observable<String> updateAuthorizationHeaderValue()
	{
		return updateAuthorizationHeaderValue( false );
	}

	public Observable<String> updateAuthorizationHeaderValue( boolean forceUpdate )
	{
		// IonLog.d( "IonConfig", "UpdateAuthorization: authorizationHeaderValue != null: " + ( authorizationHeaderValue != null ) + ", call != null: " + ( authorizationHeaderValueCall != null ) );

		if ( authorizationHeaderValueCall == null || ( authorizationHeaderValue != null && !forceUpdate ) )
		{
			return Observable.just( authorizationHeaderValue );
		}

		if ( !forceUpdate )
		{
			String authorizationFromCache = authorizations.get( this );
			if ( authorizationFromCache != null )
			{
				return Observable.just( authorizationFromCache );
			}
		}

		Observable<String> updatedAuthorization = authorizationHeaderValueCall
				.map( authorizationHeaderValue -> {
					authorizations.put( IonConfig.this, authorizationHeaderValue );
					this.authorizationHeaderValue = authorizationHeaderValue;
					return authorizationHeaderValue;
				} )
				.doOnNext( authorizationHeaderValue -> pendingLogins.finished( this ) );
		return pendingLogins.starting( this, updatedAuthorization );
	}

	public String getAuthorizationHeaderValue()
	{
		if ( authorizationHeaderValue == null && authorizationHeaderValueCall != null )
		{
			authorizationHeaderValue = authorizations.get( this );
		}
		return authorizationHeaderValue;
	}
}
