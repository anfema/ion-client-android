package com.anfema.ampclient.service.models;

public class Login
{
	public String api_url;

	public String token;

	public String user;

	public String getApi_url()
	{
		return api_url;
	}

	public void setApi_url( String api_url )
	{
		this.api_url = api_url;
	}

	public String getToken()
	{
		return token;
	}

	public void setToken( String token )
	{
		this.token = token;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser( String user )
	{
		this.user = user;
	}

	@Override
	public String toString()
	{
		return "ClassPojo [api_url = " + api_url + ", token = " + token + ", user = " + user + "]";
	}
}