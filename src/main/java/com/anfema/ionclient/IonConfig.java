package com.anfema.ionclient;

import android.content.Context;

import com.anfema.ionclient.exceptions.IonConfigInvalidException;
import com.anfema.ionclient.utils.IonLog;
import com.anfema.ionclient.utils.MemoryUtils;
import com.anfema.ionclient.utils.PendingDownloadHandler;
import com.anfema.utils.EqualsContract;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import androidx.annotation.NonNull;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import okhttp3.Response;

public class IonConfig
{
	public static final String DEFAULT_VARIATION                        = "default";
	public static final int    DEFAULT_MINUTES_UNTIL_COLLECTION_REFETCH = 5;
	public static final int    DEFAULT_NETWORK_TIMEOUT                  = -1;
	public static final int    CALC_REASONABLE_SIZE                     = -1;

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
	 * Size of LRU memory cache (for all client instances accumulated). The Unit is bytes.
	 * <p>
	 * Value must be overwritten before first ION request is made, otherwise it won't have any effect.
	 * It is recommended to set it as early as possible, e.g. in onCreate() of application (or first activity).
	 * <p>
	 * Be careful not to exceed the available RAM of the application. You might want to define the memory cache size as a fraction of the
	 * available space. Therefore, you can use {@link MemoryUtils#calculateAvailableMemCache(Context)}.
	 * <p>
	 * If not set to a positive value, default cache size will be used.
	 */
	public static int pagesMemCacheSize = CALC_REASONABLE_SIZE;

	/**
	 * Maximum size of memory cache, which all picasso instances share. The Unit is bytes.
	 * <p>
	 * Value must be overwritten before first ION request is made, otherwise it won't have any effect.
	 * It is recommended to set it as early as possible, e.g. in onCreate() of application (or first activity).
	 * <p>
	 * Be careful not to exceed the available RAM of the application. You might want to define the memory cache size as a fraction of the
	 * available space. Therefore, you can use {@link MemoryUtils#calculateAvailableMemCache(Context)}.
	 * <p>
	 * If not set to a positive value, default cache size will be used.
	 */
	public static int picassoMemCacheSize = CALC_REASONABLE_SIZE;

	/**
	 * Set log level for ION client.
	 */
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
	private final Single<String> authorizationHeaderValueCall;

	/**
	 * Add customer headers - besides the 'Authorization' header
	 */
	@NonNull
	public final Map<String, String> additionalHeaders;

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

	/**
	 * Unit: seconds
	 * If set to DEFAULT_NETWORK_TIMEOUT, the default timeouts from okhttp library are used (10 seconds).
	 */
	public final int networkTimeout;

	@SuppressWarnings("unused")
	public static class Builder
	{
		private final String              baseUrl;
		private final String              collectionIdentifier;
		private       String              locale;
		private       String              variation                     = DEFAULT_VARIATION;
		private       String              authorizationHeaderValue      = null;
		private       Single<String>      authorizationHeaderValueCall  = null;
		@NonNull
		private final Map<String, String> additionalHeaders             = new HashMap<>();
		private       boolean             archiveDownloads              = false;
		private       boolean             ftsDbDownloads                = false;
		private       int                 minutesUntilCollectionRefetch = DEFAULT_MINUTES_UNTIL_COLLECTION_REFETCH;
		private       int                 networkTimeout                = DEFAULT_NETWORK_TIMEOUT;


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

		public Builder authorization( Single<String> authorizationHeaderValueCall )
		{
			this.authorizationHeaderValueCall = authorizationHeaderValueCall;
			return this;
		}

		public Builder addHeader( @NonNull String key, @NonNull String value )
		{
			this.additionalHeaders.put( key, value );
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

		public Builder networkTimeout( int networkTimeout )
		{
			this.networkTimeout = networkTimeout;
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
			return new IonConfig(
					baseUrl,
					collectionIdentifier,
					locale,
					variation,
					authorizationHeaderValue,
					authorizationHeaderValueCall,
					additionalHeaders,
					archiveDownloads,
					ftsDbDownloads,
					minutesUntilCollectionRefetch,
					networkTimeout
			);
		}
	}

	public IonConfig(
			String baseUrl,
			String collectionIdentifier,
			String locale,
			String variation,
			String authorizationHeaderValue,
			Single<String> authorizationHeaderValueCall,
			@NonNull Map<String, String> additionalHeaders,
			boolean archiveDownloads,
			boolean ftsDbDownloads,
			int minutesUntilCollectionRefetch,
			int networkTimeout
	)
	{
		this.baseUrl = baseUrl;
		this.collectionIdentifier = collectionIdentifier;
		this.locale = locale;
		this.variation = variation;
		this.authorizationHeaderValue = authorizationHeaderValue;
		this.authorizationHeaderValueCall = authorizationHeaderValueCall;
		this.additionalHeaders = additionalHeaders;
		this.archiveDownloads = archiveDownloads;
		this.ftsDbDownloads = ftsDbDownloads;
		this.minutesUntilCollectionRefetch = minutesUntilCollectionRefetch;
		this.networkTimeout = networkTimeout;
	}

	public IonConfig( @NonNull IonConfig otherConfig )
	{
		this.baseUrl = otherConfig.baseUrl;
		this.collectionIdentifier = otherConfig.collectionIdentifier;
		this.locale = otherConfig.locale;
		this.variation = otherConfig.variation;
		this.authorizationHeaderValue = otherConfig.authorizationHeaderValue;
		this.authorizationHeaderValueCall = otherConfig.authorizationHeaderValueCall;
		this.additionalHeaders = otherConfig.additionalHeaders;
		this.archiveDownloads = otherConfig.archiveDownloads;
		this.ftsDbDownloads = otherConfig.ftsDbDownloads;
		this.minutesUntilCollectionRefetch = otherConfig.minutesUntilCollectionRefetch;
		this.networkTimeout = otherConfig.networkTimeout;
	}

	public boolean isValid()
	{
		return baseUrl != null && baseUrl.contains( "://" )
				&& collectionIdentifier != null
				&& locale != null && locale.length() > 0
				&& ( authorizationHeaderValue != null || authorizationHeaderValueCall != null );
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
	public final boolean equals( Object obj )
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
		return EqualsContract.equal( baseUrl, other.baseUrl )
				&& EqualsContract.equal( collectionIdentifier, other.collectionIdentifier )
				&& EqualsContract.equal( locale, other.locale )
				&& EqualsContract.equal( variation, other.variation );
	}

	@Override
	public final int hashCode()
	{
		Object[] hashRelevantFields = { baseUrl, collectionIdentifier, locale, variation };
		return Arrays.hashCode( hashRelevantFields );
	}

	/**
	 * @param <T>         return type of the request
	 * @param requestFunc the in parameter is the authorization header value
	 * @return single of request is forwarded
	 */
	public <T> Single<T> authenticatedRequest( Callable<Single<T>> requestFunc )
	{
		return authenticatedRequest( authorizationHeaderValue -> requestFunc.call() );
	}

	/**
	 * @param <T>         return type of the request
	 * @param requestFunc the in parameter is the authorization header value
	 * @return observable of request is forwarded
	 */
	public <T> Single<T> authenticatedRequest( Function<String, Single<T>> requestFunc )
	{
		return authenticatedRequest( requestFunc, true );
	}

	private <T> Single<T> authenticatedRequest( Function<String, Single<T>> requestFunc, boolean tryAgain )
	{
		return updateAuthorizationHeaderValue( !tryAgain )
				.flatMap( requestFunc )
				.flatMap( response ->
				{
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
					return Single.just( response );
				} );
	}

	public Single<String> updateAuthorizationHeaderValue()
	{
		return updateAuthorizationHeaderValue( false );
	}

	public Single<String> updateAuthorizationHeaderValue( boolean forceUpdate )
	{
		// IonLog.d( "IonConfig", "UpdateAuthorization: authorizationHeaderValue != null: " + ( authorizationHeaderValue != null ) + ", call != null: " + ( authorizationHeaderValueCall != null ) );

		if ( authorizationHeaderValueCall == null || ( authorizationHeaderValue != null && !forceUpdate ) )
		{
			return Single.just( authorizationHeaderValue );
		}

		if ( !forceUpdate )
		{
			String authorizationFromCache = authorizations.get( this );
			if ( authorizationFromCache != null )
			{
				return Single.just( authorizationFromCache );
			}
		}

		Single<String> updatedAuthorization = authorizationHeaderValueCall
				.map( authorizationHeaderValue ->
				{
					authorizations.put( IonConfig.this, authorizationHeaderValue );
					this.authorizationHeaderValue = authorizationHeaderValue;
					return authorizationHeaderValue;
				} )
				.doOnSuccess( authorizationHeaderValue -> pendingLogins.finished( this ) );
		return pendingLogins.starting( this, updatedAuthorization.toObservable() ).singleOrError();
	}

	public String getAuthorizationHeaderValue()
	{
		if ( authorizationHeaderValue == null && authorizationHeaderValueCall != null )
		{
			authorizationHeaderValue = authorizations.get( this );
		}
		return authorizationHeaderValue;
	}

	public void clearCachedAuthorization()
	{
		authorizations.remove( this );
	}

	public static void clearEntireAuthorizationCache()
	{
		authorizations.clear();
	}
}
