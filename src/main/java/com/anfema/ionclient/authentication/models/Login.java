package com.anfema.ionclient.authentication.models;

/**
 * Response of a successful authentication with username and password contains this information.
 */
public class Login
{
	/**
	 * Points to the the endpoint where ION is available at.
	 */
	public String api_url;

	/**
	 * Essential for further requests on ION. Required header is "Authorization: token [token]".
	 */
	public String token;

	/**
	 * Id of the user
	 */
	public String user;

	@Override
	public String toString()
	{
		return "Login [api_url = " + api_url + ", token = " + token + ", user = " + user + "]";
	}
}