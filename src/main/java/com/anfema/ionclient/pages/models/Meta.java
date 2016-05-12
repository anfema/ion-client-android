package com.anfema.ionclient.pages.models;

import android.support.annotation.NonNull;
import android.webkit.URLUtil;

import com.anfema.ionclient.pages.models.contents.Connection;
import com.anfema.ionclient.pages.models.contents.Downloadable;
import com.anfema.ionclient.pages.models.contents.DownloadableResource;
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
	 * Raw, unparsed JSON data of meta
	 */
	public Map<String, JsonElement> json;

	/**
	 * Checks whether a non-null value exists for an outlet identifier in meta data
	 *
	 * @param outlet identifier of outlet available in page meta data (e.g. "title" or "thumbnail")
	 * @return true if value exists for outlet identifier and is not null
	 */
	public boolean contains( String outlet )
	{
		return getRaw( outlet ) != null;
	}

	/**
	 * Checks whether a value exists for for an outlet identifier in meta data, returns true even if the value is null
	 *
	 * @param outlet identifier of outlet available in page meta data (e.g. "title" or "thumbnail")
	 * @return true if value exists for outlet identifier, value can be null
	 */
	public boolean containsAllowNullValue( String outlet )
	{
		return json != null && json.containsKey( outlet );
	}

	/**
	 * Access meta data when value is not string based and none of the other access methods apply.
	 *
	 * @param outlet identifier of outlet available in page meta data (e.g. "title" or "thumbnail")
	 * @return value is a {@link JsonElement} and needs to be deserialized.
	 */
	public JsonElement getRaw( String outlet )
	{
		if ( json == null )
		{
			return null;
		}
		return json.get( outlet );
	}

	/**
	 * Convenience method to obtain the text of a {@link com.anfema.ionclient.pages.models.contents.TextContent} available in page's meta data.
	 * <p>
	 * This method guesses the mime-type and formats the text respectively.
	 *
	 * @param outlet identifier of outlet available in page meta data (e.g. "title" or "thumbnail")
	 * @return formatted text
	 * @throws NullPointerException if data does not exist
	 * @throws JsonSyntaxException  if data is not valid
	 */
	public CharSequence getTextOrThrow( String outlet ) throws JsonSyntaxException, NullPointerException
	{
		return TextFormatting.format( getStringOrThrow( outlet ) );
	}

	/**
	 * Convenience method to obtain the text of a {@link com.anfema.ionclient.pages.models.contents.TextContent} available in page's meta data.
	 * <p>
	 * This method guesses the mime-type and formats the text respectively.
	 *
	 * @param outlet identifier of outlet available in page meta data (e.g. "title" or "thumbnail")
	 * @return formatted text if exists, {@code null} otherwise
	 */
	public CharSequence getTextOrNull( String outlet )
	{
		String text = getStringOrNull( outlet );
		return text != null ? TextFormatting.format( text ) : null;
	}

	/**
	 * Convenience method to obtain the text of a {@link com.anfema.ionclient.pages.models.contents.TextContent} available in page's meta data.
	 * <p>
	 * This method guesses the mime-type and formats the text respectively.
	 *
	 * @param outlet identifier of outlet available in page meta data (e.g. "title" or "thumbnail")
	 * @return formatted text if exists, empty string otherwise
	 */
	@NonNull
	public CharSequence getTextOrEmpty( String outlet )
	{
		CharSequence text = getTextOrNull( outlet );
		return text != null ? text : "";
	}

	/**
	 * Convenience method to obtain the text of a {@link com.anfema.ionclient.pages.models.contents.TextContent} available in page's meta data.
	 * <p>
	 * Formatting tags are removed if mime-type (most probably) is not "text/plain".
	 *
	 * @param outlet identifier of outlet available in page meta data (e.g. "title" or "thumbnail")
	 * @return text without formatting
	 * @throws NullPointerException if data does not exist
	 * @throws JsonSyntaxException  if data is not valid
	 */
	public String getPlainTextOrThrow( String outlet ) throws JsonSyntaxException, NullPointerException
	{
		return GsonHolder.getInstance().fromJson( json.get( outlet ), String.class );
	}

	/**
	 * Convenience method to obtain the text of a {@link com.anfema.ionclient.pages.models.contents.TextContent} available in page's meta data.
	 * <p>
	 * Formatting tags are removed if mime-type (most probably) is not "text/plain".
	 *
	 * @param outlet identifier of outlet available in page meta data (e.g. "title" or "thumbnail")
	 * @return text without formatting if exists, {@code null} otherwise
	 */
	public String getPlainTextOrNull( String outlet )
	{
		CharSequence text = getTextOrNull( outlet );
		return text != null ? text.toString() : null;
	}

	/**
	 * Convenience method to obtain the text of a {@link com.anfema.ionclient.pages.models.contents.TextContent} available in page's meta data.
	 * <p>
	 * Formatting tags are removed if mime-type (most probably) is not "text/plain".
	 *
	 * @param outlet identifier of outlet available in page meta data (e.g. "title" or "thumbnail")
	 * @return text without formatting if exists, empty string otherwise
	 */
	@NonNull
	public String getPlainTextOrEmpty( String outlet )
	{
		return getTextOrEmpty( outlet ).toString();
	}

	/**
	 * Get raw content string from page's meta data.
	 * If you want text, which does not contain formatting texts, better use {@link #getTextOrEmpty(String)} or {@link #getPlainTextOrEmpty(String)}
	 *
	 * @param outlet identifier of outlet available in page meta data (e.g. "title" or "thumbnail")
	 * @return string value for outlet identifier
	 * @throws NullPointerException if data does not exist
	 * @throws JsonSyntaxException  if data is not valid
	 */
	private String getStringOrThrow( String outlet ) throws JsonSyntaxException, NullPointerException
	{
		return GsonHolder.getInstance().fromJson( json.get( outlet ), String.class );
	}

	/**
	 * Get raw content string from page's meta data.
	 * If you want text, which does not contain formatting texts, better use {@link #getTextOrEmpty(String)} or {@link #getPlainTextOrEmpty(String)}
	 *
	 * @param outlet identifier of outlet available in page meta data (e.g. "title" or "thumbnail")
	 * @return string value for outlet identifier if exists in meta data, {@code null} otherwise
	 */
	private String getStringOrNull( String outlet )
	{
		if ( json == null || !json.containsKey( outlet ) )
		{
			return null;
		}
		try
		{
			return getStringOrThrow( outlet );
		}
		catch ( JsonSyntaxException e )
		{
			return null;
		}
	}

	/**
	 * Use to access a list of strings from page's meta data.
	 * Be aware that strings are raw data in meta representing different content types. It makes probably sense to parse it into the desired format.
	 * If you expect texts, which can contain formatting tags, call {@link TextFormatting#format(String)} on each string.
	 * If you expect dates, call {@link DateTimeUtils#parseOrNull(String)}.
	 * If you expect a cross-link, use {@link Connection#Connection(String)}.
	 *
	 * @param outlet identifier of outlet available in page meta data
	 * @return values for outlet identifier as raw strings
	 * @throws NullPointerException if data does not exist
	 * @throws JsonSyntaxException  if data is not valid
	 */
	public List<String> getStringListOrThrow( String outlet ) throws JsonSyntaxException, NullPointerException
	{
		Type listType = new TypeToken<List<String>>()
		{
		}.getType();
		return GsonHolder.getInstance().fromJson( json.get( outlet ), listType );
	}

	/**
	 * Use to access a list of strings from page's meta data.
	 * Be aware that strings are raw data in meta representing different content types. It makes probably sense to parse it into the desired format.
	 * If you expect texts, which can contain formatting tags, call {@link TextFormatting#format(String)} on each string.
	 * If you expect dates, call {@link DateTimeUtils#parseOrNull(String)}.
	 * If you expect a cross-link, use {@link Connection#Connection(String)}.
	 *
	 * @param outlet identifier of outlet available in page meta data
	 * @return values for outlet identifier as raw strings if exists in meta data, empty list otherwise
	 */
	public List<String> getStringListOrEmpty( String outlet )
	{
		if ( json == null || !json.containsKey( outlet ) )
		{
			return new ArrayList<>();
		}
		try
		{
			Type listType = new TypeToken<List<String>>()
			{
			}.getType();
			return GsonHolder.getInstance().fromJson( json.get( outlet ), listType );
		}
		catch ( JsonSyntaxException e )
		{
			return new ArrayList<>();
		}
	}

	/**
	 * Parse connection from string
	 *
	 * @param outlet JSON property name within "json" object
	 * @return link to another collection, page, content
	 * @throws NullPointerException if data does not exist
	 * @throws JsonSyntaxException  if data is not valid
	 */
	public DateTime getDateTimeOrThrow( String outlet ) throws JsonSyntaxException, NullPointerException, IllegalArgumentException
	{
		return DateTimeUtils.parseOrThrow( getStringOrThrow( outlet ) );
	}

	/**
	 * Parse connection from string
	 *
	 * @param outlet JSON property name within "json" object
	 * @return @return link to another collection, page, content if data exists, {@code null} otherwise
	 */
	public DateTime getDateTimeOrNull( String outlet )
	{
		return DateTimeUtils.parseOrNull( getStringOrNull( outlet ) );
	}

	/**
	 * Parse connection from string
	 *
	 * @param outlet JSON property name within "json" object
	 * @return link to another collection, page, content
	 * @throws NullPointerException if data does not exist
	 * @throws JsonSyntaxException  if data is not valid
	 */
	public Connection getConnectionOrThrow( String outlet ) throws JsonSyntaxException, NullPointerException
	{
		return new Connection( getStringOrThrow( outlet ) );
	}

	/**
	 * Parse connection from string
	 *
	 * @param outlet JSON property name within "json" object
	 * @return @return link to another collection, page, content if data exists, {@code null} otherwise
	 */
	public Connection getConnectionOrNull( String outlet )
	{
		return new Connection( getStringOrNull( outlet ) );
	}

	/**
	 * Get downloadable content
	 *
	 * @param outlet outlet identifier to identify content
	 * @return content if it is a {@link Downloadable} content, or {@code null} otherwise
	 */
	public Downloadable getDownloadableOrNull( String outlet )
	{
		String url = getStringOrNull( outlet );
		if ( url == null || !URLUtil.isValidUrl( url ) )
		{
			return null;
		}
		return new DownloadableResource( url );
	}

	/**
	 * Convenience method to compare values from json with other ones.
	 *
	 * @param outlet       JSON property name within "json" object (e.g. "title" or "thumbnail")
	 * @param compareValue A value to compare the concerning json value with.
	 * @return true, if json value exists and equals {@param compareValue}
	 */
	public boolean textEquals( String outlet, String compareValue ) throws JsonSyntaxException
	{
		if ( json == null || !json.containsKey( outlet ) )
		{
			return false;
		}

		try
		{
			String text = getPlainTextOrThrow( outlet );
			if ( text == null )
			{
				return compareValue == null;
			}
			return text.equals( compareValue );
		}
		catch ( JsonSyntaxException e )
		{
			throw new JsonSyntaxException( "You tried to compare " + compareValue + " with a meta entry " + json.get( outlet ) + " which could not be parsed to a String.", e );
		}
	}
}
