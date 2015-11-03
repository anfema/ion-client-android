package com.anfema.ampclient.service.models;

public class LoginResponse
{
	public Login login;

	public Login getLogin()
	{
		return login;
	}

	public void setLogin( Login login )
	{
		this.login = login;
	}

	/**
	 * for convenient access to API token
	 */
	public String getToken()
	{
		return login.getToken();
	}

	@Override
	public String toString()
	{
		return "ClassPojo [login = " + login + "]";
	}
}
