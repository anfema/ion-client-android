package com.anfema.ampclient.service.models;

public class Login
{
	public String api_url;

	public String token;

	public String user;

	@Override
	public String toString()
	{
		return "Login [api_url = " + api_url + ", token = " + token + ", user = " + user + "]";
	}
}