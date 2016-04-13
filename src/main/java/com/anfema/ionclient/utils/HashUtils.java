package com.anfema.ionclient.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils
{
	public static final int BUFFER_SIZE = 2048;

	public static String getMD5Hash( String input )
	{
		return getDigest( input, HashAlgorithm.MD5 );
	}

	public static String getSha256( File file )
	{
		return getDigest( file, HashAlgorithm.SHA_256 );
	}

	public static String getDigest( File file, HashAlgorithm algorithm )
	{
		try
		{
			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream( file );
				MessageDigest md = MessageDigest.getInstance( algorithm.toString() );
				DigestInputStream dis = new DigestInputStream( fis, md );
				byte[] buffer = new byte[ BUFFER_SIZE ];
				//noinspection StatementWithEmptyBody
				while ( dis.read( buffer ) != -1 )
				{
					// work is done in condition
				}
				dis.close();
				return getDigestString( md, algorithm );
			}
			finally
			{
				if ( fis != null )
				{
					fis.close();
				}
			}
		}
		catch ( IOException | NoSuchAlgorithmException e )
		{
			return error( e );
		}
	}

	public static String getDigest( String text, HashAlgorithm algorithm )
	{
		try
		{
			return getDigest( text.getBytes( "UTF-8" ), algorithm );
		}
		catch ( UnsupportedEncodingException e )
		{
			return error( e );
		}
	}

	public static String getDigest( byte[] bytes, HashAlgorithm algorithm )
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance( algorithm.toString() );
			md.update( bytes );
			return getDigestString( md, algorithm );
		}
		catch ( NoSuchAlgorithmException e )
		{
			return error( e );
		}
	}

	private static String getDigestString( MessageDigest md, HashAlgorithm algorithm )
	{
		byte[] digest = md.digest();
		String pattern = "%0" + algorithm.length() + "x";
		return String.format( pattern, new BigInteger( 1, digest ) );
	}

	private static String error( Exception e )
	{
		Log.ex( "HashUtils", e );
		return "-1";
	}
}
