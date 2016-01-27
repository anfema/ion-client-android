package com.anfema.ampclient.utils;

public enum HashAlgorithm
{
	MD5
			{
				@Override
				public int length()
				{
					return 32;
				}

				@Override
				public String toString()
				{
					return "MD5";
				}
			},
	SHA_256
			{
				@Override
				public int length()
				{
					return 64;
				}

				@Override
				public String toString()
				{
					return "SHA-256";
				}
			};

	public abstract int length();
}
