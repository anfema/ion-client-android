package com.anfema.ampclient.authorization;

import android.util.Base64;

import com.anfema.ampclient.utils.Log;

import java.io.UnsupportedEncodingException;

/**
 * Helper class for basic access authentication.
 */
public class BasicAuth
{
	/**
	 * Generates the value for the "Authorization" header.
	 */
	public static String getAuthHeaderValue( String username, String password )
	{
		String usernamePassword = username + ":" + password;
		byte[] data = null;
		try
		{
			data = usernamePassword.getBytes( "UTF-8" );
		}
		catch ( UnsupportedEncodingException e )
		{
			Log.ex( new Exception( "Could not get bytes of " + usernamePassword, e ) );
		}
		String base64 = Base64.encodeToString( data, Base64.NO_WRAP );
		return "Basic " + base64;
	}
}
