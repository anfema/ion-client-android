package com.anfema.ampclient.callbackapproach;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.anfema.ampclient.service.AmpApiCallbacks;
import com.anfema.ampclient.service.AmpApiFactory;

public class AmpClientConfigCallbacks
{
	// keys for shared preferences
	public static final String PREFS_NAME                  = "prefs_amp_client";
	public static final String PREFS_BASE_URL              = "base_url";
	public static final String PREFS_API_TOKEN             = "prefs_api_token";
	public static final String PREFS_COLLECTION_IDENTIFIER = "prefs_collection_identifier";

	private Context         context;
	private AmpApiCallbacks ampApi;
	private String          baseUrl;
	private String          apiToken;
	private String          collectionIdentifier;
	// TODO add Locale, add variation, add caching strategy ?

	AmpClientConfigCallbacks( Context context, String baseUrl, String apiToken, String collectionIdentifier )
	{
		this.context = context;
		ampApi = AmpApiFactory.newInstance( baseUrl, AmpApiCallbacks.class );
		setBaseUrl( baseUrl );
		setApiToken( apiToken );
		setCollectionIdentifier( collectionIdentifier );
	}

	public AmpClientConfigCallbacks( Context context )
	{
		this.context = context;
	}

	public AmpApiCallbacks getAmpApi()
	{
		if ( ampApi == null )
		{
			ampApi = AmpApiFactory.newInstance( getBaseUrl(), AmpApiCallbacks.class );
		}
		return ampApi;
	}

	public String getBaseUrl()
	{
		if ( baseUrl == null )
		{
			SharedPreferences prefs = getPrefs();
			baseUrl = prefs.getString( PREFS_BASE_URL, null );
		}
		return baseUrl;
	}

	private void setBaseUrl( String baseUrl )
	{
		Editor editor = getEditor();
		editor.putString( PREFS_BASE_URL, baseUrl ).apply();
		this.baseUrl = baseUrl;
	}

	public String getApiToken()
	{
		if ( apiToken == null )
		{
			SharedPreferences prefs = getPrefs();
			apiToken = prefs.getString( PREFS_API_TOKEN, null );
		}
		return apiToken;
	}

	public String getAuthHeaderValue()
	{
		return "token " + getApiToken();
	}

	void setApiToken( String apiToken )
	{
		Editor editor = getEditor();
		editor.putString( PREFS_API_TOKEN, apiToken ).apply();
		this.apiToken = apiToken;
	}

	public String getCollectionIdentifier()
	{
		if ( collectionIdentifier == null )
		{
			SharedPreferences prefs = getPrefs();
			collectionIdentifier = prefs.getString( PREFS_COLLECTION_IDENTIFIER, null );
		}
		return collectionIdentifier;
	}

	private void setCollectionIdentifier( String collectionIdentifier )
	{
		Editor editor = getEditor();
		editor.putString( PREFS_COLLECTION_IDENTIFIER, collectionIdentifier ).apply();
		this.collectionIdentifier = collectionIdentifier;
	}

	Editor getEditor()
	{
		return context.getSharedPreferences( PREFS_NAME, 0 ).edit();
	}

	SharedPreferences getPrefs()
	{
		return context.getSharedPreferences( PREFS_NAME, 0 );
	}
}