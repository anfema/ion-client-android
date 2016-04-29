package com.anfema.ionclient.pages.models;

import android.support.annotation.NonNull;

import com.anfema.ionclient.pages.models.contents.Connection;
import com.anfema.ionclient.serialization.GsonHolder;
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
 * Meta/preview information for pages. It does not hold the content for a page.
 */
@SuppressWarnings("unused")
public class PagePreview implements Comparable<PagePreview>
{
	/**
	 * page identifier
	 */
	public String identifier;

	/**
	 * page identifier of the parent's page
	 */
	public String parent;

	/**
	 * date when published page has been updated the last time
	 */
	public DateTime last_changed;

	/**
	 * Position indicates the order among the pages.
	 */
	private long position;

	/**
	 * the page's layout
	 */
	public String layout;

	/**
	 * Meta contains additional, flexible data.
	 * Since it is very application-specific the provided data is stored as key-value-pairs, simply consisting of strings.
	 */
	public Map<String, JsonElement> meta;

	/**
	 * Convenience method to obtain the formatted text of a string from meta data.
	 * <p>
	 * This method guesses the mime-type and formats the text respectively.
	 *
	 * @param metaKey JSON property name within "meta" object (e.g. "title" or "thumbnail")
	 * @return formatted text
	 * @throws NullPointerException if data does not exist
	 * @throws JsonSyntaxException  if data is not valid
	 */
	public CharSequence getMetaTextOrThrow( String metaKey ) throws JsonSyntaxException, NullPointerException
	{
		return TextFormatting.format( getMetaStringOrThrow( metaKey ) );
	}

	/**
	 * Convenience method to obtain the formatted text of a string from meta data.
	 * <p>
	 * This method guesses the mime-type and formats the text respectively.
	 *
	 * @param metaKey JSON property name within "meta" object (e.g. "title" or "thumbnail")
	 * @return formatted text if exists, {@code null} otherwise
	 */
	public CharSequence getMetaTextOrNull( String metaKey )
	{
		String text = getMetaStringOrNull( metaKey );
		return text != null ? TextFormatting.format( text ) : null;
	}

	/**
	 * Convenience method to obtain the formatted text of a string from meta data.
	 * <p>
	 * This method guesses the mime-type and formats the text respectively.
	 *
	 * @param metaKey JSON property name within "meta" object (e.g. "title" or "thumbnail")
	 * @return formatted text if exists, empty string otherwise
	 */
	@NonNull
	public CharSequence getMetaTextOrEmpty( String metaKey )
	{
		CharSequence text = getMetaTextOrNull( metaKey );
		return text != null ? text : "";
	}

	/**
	 * Convenience method to obtain the text of a string from meta data.
	 * <p>
	 * Formatting tags are removed if mime-type (most probably) is not "text/plain".
	 *
	 * @param metaKey JSON property name within "meta" object (e.g. "title" or "thumbnail")
	 * @return text without formatting
	 * @throws NullPointerException if data does not exist
	 * @throws JsonSyntaxException  if data is not valid
	 */
	public String getMetaPlainTextOrThrow( String metaKey ) throws JsonSyntaxException, NullPointerException
	{
		return GsonHolder.getInstance().fromJson( meta.get( metaKey ), String.class );
	}

	/**
	 * Convenience method to obtain the text of a string from meta data.
	 * <p>
	 * Formatting tags are removed if mime-type (most probably) is not "text/plain".
	 *
	 * @param metaKey JSON property name within "meta" object (e.g. "title" or "thumbnail")
	 * @return text without formatting if exists, {@code null} otherwise
	 */
	public String getMetaPlainTextOrNull( String metaKey )
	{
		CharSequence text = getMetaTextOrNull( metaKey );
		return text != null ? text.toString() : null;
	}

	/**
	 * Convenience method to obtain the text of a string from meta data.
	 * <p>
	 * Formatting tags are removed if mime-type (most probably) is not "text/plain".
	 *
	 * @param metaKey JSON property name within "meta" object (e.g. "title" or "thumbnail")
	 * @return text without formatting if exists, empty string otherwise
	 */
	@NonNull
	public String getMetaPlainTextOrEmpty( String metaKey )
	{
		return getMetaTextOrEmpty( metaKey ).toString();
	}

	/**
	 * Parse string from meta properties
	 *
	 * @param metaKey JSON property name within "meta" object (e.g. "title" or "thumbnail")
	 * @return value for JSON property
	 * @throws NullPointerException if data does not exist
	 * @throws JsonSyntaxException  if data is not valid
	 */
	private String getMetaStringOrThrow( String metaKey ) throws JsonSyntaxException, NullPointerException
	{
		return GsonHolder.getInstance().fromJson( meta.get( metaKey ), String.class );
	}

	/**
	 * Parse string from meta properties
	 *
	 * @param metaKey JSON property name within "meta" object (e.g. "title" or "thumbnail")
	 * @return value for JSON property if exists, {@code null} otherwise
	 */
	private String getMetaStringOrNull( String metaKey )
	{
		if ( meta == null || !meta.containsKey( metaKey ) )
		{
			return null;
		}
		try
		{
			return getMetaStringOrThrow( metaKey );
		}
		catch ( JsonSyntaxException e )
		{
			return null;
		}
	}

	/**
	 * Parse list of strings from meta properties
	 *
	 * @param metaKey JSON property name within "meta" object
	 * @return value for JSON property
	 * @throws NullPointerException if data does not exist
	 * @throws JsonSyntaxException  if data is not valid
	 */
	public List<String> getMetaListOrThrow( String metaKey ) throws JsonSyntaxException, NullPointerException
	{
		Type listType = new TypeToken<List<String>>()
		{
		}.getType();
		return GsonHolder.getInstance().fromJson( meta.get( metaKey ), listType );
	}

	/**
	 * Parse list of strings from meta properties
	 *
	 * @param metaKey JSON property name within "meta" object
	 * @return value for JSON property if exists, {@code null} otherwise
	 */
	public List<String> getMetaListOrEmpty( String metaKey )
	{
		if ( meta == null || !meta.containsKey( metaKey ) )
		{
			return new ArrayList<>();
		}
		try
		{
			Type listType = new TypeToken<List<String>>()
			{
			}.getType();
			return GsonHolder.getInstance().fromJson( meta.get( metaKey ), listType );
		}
		catch ( JsonSyntaxException e )
		{
			return new ArrayList<>();
		}
	}

	/**
	 * Parse connection from string
	 *
	 * @param metaKey JSON property name within "meta" object
	 * @return link to another collection, page, content
	 * @throws NullPointerException if data does not exist
	 * @throws JsonSyntaxException  if data is not valid
	 */
	public Connection getMetaConnectionOrThrow( String metaKey ) throws JsonSyntaxException, NullPointerException
	{
		return new Connection( getMetaStringOrThrow( metaKey ) );
	}

	/**
	 * Parse connection from string
	 *
	 * @param metaKey JSON property name within "meta" object
	 * @return @return link to another collection, page, content if data exists, {@code null} otherwise
	 */
	public Connection getMetaConnectionOrNull( String metaKey )
	{
		return new Connection( getMetaStringOrNull( metaKey ) );
	}

	/**
	 * Convenience method to compare values from meta with other ones.
	 *
	 * @param metaKey      JSON property name within "meta" object (e.g. "title" or "thumbnail")
	 * @param compareValue A value to compare the concerning meta value with.
	 * @return true, if meta value exists and equals {@param compareValue}
	 */
	public boolean metaTextEquals( String metaKey, String compareValue ) throws JsonSyntaxException
	{
		if ( meta == null || !meta.containsKey( metaKey ) )
		{
			return false;
		}

		try
		{
			String text = getMetaPlainTextOrThrow( metaKey );
			if ( text == null )
			{
				return compareValue == null;
			}
			return text.equals( compareValue );
		}
		catch ( JsonSyntaxException e )
		{
			throw new JsonSyntaxException( "You tried to compare " + compareValue + " with a meta entry " + meta.get( metaKey ) + " which could not be parsed to a String.", e );
		}
	}

	/**
	 * Sort by positions ascending
	 */
	@Override
	public int compareTo( PagePreview another )
	{
		if ( another == null )
		{
			// null objects shall be at the end
			return -1;
		}
		// Alternatively, one long could be subtracted from the other resulting in a long in the right range. However, casting to int might not be safe.
		if ( position == another.position )
		{
			return 0;
		}
		return position < another.position ? -1 : 1;
	}

	@Override
	public String toString()
	{
		return "PagePreview [identifier = " + identifier + ", parent = " + parent + ", last_changed = " + last_changed + ", position = " + position + ", layout = " + layout + "]";
	}
}
