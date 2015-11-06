package com.anfema.ampclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.anfema.ampclient.service.AmpApi;
import com.anfema.ampclient.service.AmpApiFactory;

public class AmpClientConfig
{
	// keys for shared preferences
	public static final String PREFS_NAME                  = "prefs_amp_client";
	public static final String PREFS_BASE_URL              = "base_url";
	public static final String PREFS_API_TOKEN             = "prefs_api_token";
	public static final String PREFS_COLLECTION_IDENTIFIER = "prefs_collection_identifier";

	private Context appContext;
	private AmpApi  ampApi;
	private String  baseUrl;
	private String  apiToken;
	private String  collectionIdentifier;

	public AmpClientConfig( Context appContext, String baseUrl, String apiToken, String collectionIdentifier )
	{
		this.appContext = appContext;
		ampApi = AmpApiFactory.newInstance( baseUrl );
		setBaseUrl( baseUrl );
		setApiToken( apiToken );
		setCollectionIdentifier( collectionIdentifier );
	}

	public AmpClientConfig( Context appContext )
	{
		this.appContext = appContext;
	}

	public AmpApi getAmpApi()
	{
		if ( ampApi == null )
		{
			ampApi = AmpApiFactory.newInstance( getBaseUrl() );
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

	private void setApiToken( String apiToken )
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
		return appContext.getSharedPreferences( PREFS_NAME, 0 ).edit();
	}

	SharedPreferences getPrefs()
	{
		return appContext.getSharedPreferences( PREFS_NAME, 0 );
	}
}