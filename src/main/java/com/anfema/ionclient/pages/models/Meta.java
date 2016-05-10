package com.anfema.ionclient.pages.models;

import android.support.annotation.NonNull;

import com.anfema.ionclient.pages.models.contents.Connection;
import com.anfema.ionclient.serialization.GsonHolder;
import com.anfema.ionclient.utils.DateTimeUtils;
import com.anfema.ionclient.utils.TextFormatting;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Meta information of page previews. Meta enriches page previes with additional, flexible data.
 * Since it is very application-specific the provided data is stored as key-value-pairs, simply consisting of strings.
 */
@SuppressWarnings("unused")
public class Meta
{
	/**
	 * Unparsed JSON data of meta
	 */
	public Map<String, JsonElement> json;

	/**
	 * Checks whether a non-null value exists for the key
	 */
	public boolean contains( String key )
	{
		return getRaw( key ) != null;
	}

	/**
	 * Checks whether a value exists for the key, returns true even if the value is null
	 */
	public boolean containsAllowNullValue( String key )
	{
		return json != null && json.containsKey( key );
	}

	public JsonElement getRaw( String key )
	{
		if ( json == null )
		{
			return null;
		}
		return json.get( key );
	}

	/**
	 * Convenience method to obtain the formatted text of a string from json data.
	 * <p>
	 * This method guesses the mime-type and formats the text respectively.
	 *
	 * @param metaKey JSON property name within "json" object (e.g. "title" or "thumbnail")
	 * @return formatted text
	 * @throws NullPointerException if data does not exist
	 * @throws JsonSyntaxException  if data is not valid
	 */
	public CharSequence getTextOrThrow( String metaKey ) throws JsonSyntaxException, NullPointerException
	{
		return TextFormatting.format( getStringOrThrow( metaKey ) );
	}

	/**
	 * Convenience method to obtain the formatted text of a string from json data.
	 * <p>
	 * This method guesses the mime-type and formats the text respectively.
	 *
	 * @param metaKey JSON property name within "json" object (e.g. "title" or "thumbnail")
	 * @return formatted text if exists, {@code null} otherwise
	 */
	public CharSequence getTextOrNull( String metaKey )
	{
		String text = getStringOrNull( metaKey );
		return text != null ? TextFormatting.format( text ) : null;
	}

	/**
	 * Convenience method to obtain the formatted text of a string from json data.
	 * <p>
	 * This method guesses the mime-type and formats the text respectively.
	 *
	 * @param metaKey JSON property name within "json" object (e.g. "title" or "thumbnail")
	 * @return formatted text if exists, empty string otherwise
	 */
	@NonNull
	public CharSequence getTextOrEmpty( String metaKey )
	{
		CharSequence text = getTextOrNull( metaKey );
		return text != null ? text : "";
	}

	/**
	 * Convenience method to obtain the text of a string from json data.
	 * <p>
	 * Formatting tags are removed if mime-type (most probably) is not "text/plain".
	 *
	 * @param metaKey JSON property name within "json" object (e.g. "title" or "thumbnail")
	 * @return text without formatting
	 * @throws NullPointerException if data does not exist
	 * @throws JsonSyntaxException  if data is not valid
	 */
	public String getPlainTextOrThrow( String metaKey ) throws JsonSyntaxException, NullPointerException
	{
		return GsonHolder.getInstance().fromJson( json.get( metaKey ), String.class );
	}

	/**
	 * Convenience method to obtain the text of a string from json data.
	 * <p>
	 * Formatting tags are removed if mime-type (most probably) is not "text/plain".
	 *
	 * @param metaKey JSON property name within "json" object (e.g. "title" or "thumbnail")
	 * @return text without formatting if exists, {@code null} otherwise
	 */
	public String getPlainTextOrNull( String metaKey )
	{
		CharSequence text = getTextOrNull( metaKey );
		return text != null ? text.toString() : null;
	}

	/**
	 * Convenience method to obtain the text of a string from json data.
	 * <p>
	 * Formatting tags are removed if mime-type (most probably) is not "text/plain".
	 *
	 * @param metaKey JSON property name within "json" object (e.g. "title" or "thumbnail")
	 * @return text without formatting if exists, empty string otherwise
	 */
	@NonNull
	public String getPlainTextOrEmpty( String metaKey )
	{
		return getTextOrEmpty( metaKey ).toString();
	}

	/**
	 * Parse string from json properties
	 *
	 * @param metaKey JSON property name within "json" object (e.g. "title" or "thumbnail")
	 * @return value for JSON property
	 * @throws NullPointerException if data does not exist
	 * @throws JsonSyntaxException  if data is not valid
	 */
	private String getStringOrThrow( String metaKey ) throws JsonSyntaxException, NullPointerException
	{
		return GsonHolder.getInstance().fromJson( json.get( metaKey ), String.class );
	}

	/**
	 * Parse string from json properties
	 *
	 * @param metaKey JSON property name within "json" object (e.g. "title" or "thumbnail")
	 * @return value for JSON property if exists, {@code null} otherwise
	 */
	private String getStringOrNull( String metaKey )
	{
		if ( json == null || !json.containsKey( metaKey ) )
		{
			return null;
		}
		try
		{
			return getStringOrThrow( metaKey );
		}
		catch ( JsonSyntaxException e )
		{
			return null;
		}
	}

	/**
	 * Parse list of strings from json properties
	 *
	 * @param metaKey JSON property name within "json" object
	 * @return value for JSON property
	 * @throws NullPointerException if data does not exist
	 * @throws JsonSyntaxException  if data is not valid
	 */
	public List<String> getListOrThrow( String metaKey ) throws JsonSyntaxException, NullPointerException
	{
		Type listType = new TypeToken<List<String>>()
		{
		}.getType();
		return GsonHolder.getInstance().fromJson( json.get( metaKey ), listType );
	}

	/**
	 * Parse list of strings from json properties
	 *
	 * @param metaKey JSON property name within "json" object
	 * @return value for JSON property if exists, {@code null} otherwise
	 */
	public List<String> getListOrEmpty( String metaKey )
	{
		if ( json == null || !json.containsKey( metaKey ) )
		{
			return new ArrayList<>();
		}
		try
		{
			Type listType = new TypeToken<List<String>>()
			{
			}.getType();
			return GsonHolder.getInstance().fromJson( json.get( metaKey ), listType );
		}
		catch ( JsonSyntaxException e )
		{
			return new ArrayList<>();
		}
	}

	/**
	 * Parse connection from string
	 *
	 * @param metaKey JSON property name within "json" object
	 * @return link to another collection, page, content
	 * @throws NullPointerException if data does not exist
	 * @throws JsonSyntaxException  if data is not valid
	 */
	public DateTime getDateTimeOrThrow( String metaKey ) throws JsonSyntaxException, NullPointerException, IllegalArgumentException
	{
		return DateTimeUtils.parseOrThrow( getStringOrThrow( metaKey ) );
	}

	/**
	 * Parse connection from string
	 *
	 * @param metaKey JSON property name within "json" object
	 * @return @return link to another collection, page, content if data exists, {@code null} otherwise
	 */
	public DateTime getDateTimeOrNull( String metaKey )
	{
		return DateTimeUtils.parseOrNull( getStringOrNull( metaKey ) );
	}

	/**
	 * Parse connection from string
	 *
	 * @param metaKey JSON property name within "json" object
	 * @return link to another collection, page, content
	 * @throws NullPointerException if data does not exist
	 * @throws JsonSyntaxException  if data is not valid
	 */
	public Connection getConnectionOrThrow( String metaKey ) throws JsonSyntaxException, NullPointerException
	{
		return new Connection( getStringOrThrow( metaKey ) );
	}

	/**
	 * Parse connection from string
	 *
	 * @param metaKey JSON property name within "json" object
	 * @return @return link to another collection, page, content if data exists, {@code null} otherwise
	 */
	public Connection getConnectionOrNull( String metaKey )
	{
		return new Connection( getStringOrNull( metaKey ) );
	}

	/**
	 * Convenience method to compare values from json with other ones.
	 *
	 * @param metaKey      JSON property name within "json" object (e.g. "title" or "thumbnail")
	 * @param compareValue A value to compare the concerning json value with.
	 * @return true, if json value exists and equals {@param compareValue}
	 */
	public boolean textEquals( String metaKey, String compareValue ) throws JsonSyntaxException
	{
		if ( json == null || !json.containsKey( metaKey ) )
		{
			return false;
		}

		try
		{
			String text = getPlainTextOrThrow( metaKey );
			if ( text == null )
			{
				return compareValue == null;
			}
			return text.equals( compareValue );
		}
		catch ( JsonSyntaxException e )
		{
			throw new JsonSyntaxException( "You tried to compare " + compareValue + " with a meta entry " + json.get( metaKey ) + " which could not be parsed to a String.", e );
		}
	}
}
