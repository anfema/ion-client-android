package com.anfema.ampclient.authorization;

/**
 * Helper class for basic access authentication.
 */
public class TokenAuth
{
	/**
	 * Generates the value for the "Authorization" header.
	 */
	public static String getAuthHeaderValue( String apiToken )
	{
		return "token " + apiToken;
	}
}
