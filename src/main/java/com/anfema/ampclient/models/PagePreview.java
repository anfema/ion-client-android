package com.anfema.ampclient.models;

import com.anfema.ampclient.serialization.GsonHolder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Meta/preview information for pages. It does not hold the content for a page.
 */
public class PagePreview
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
	 * Meta contains additional, flexible data.
	 * Since it is very application-specific the provided data is stored as key-value-pairs, simply consisting of strings.
	 */
	public Map<String, JsonElement> meta;

	public String getMetaString( String metaKey ) throws JsonSyntaxException
	{
		return GsonHolder.getInstance().fromJson( meta.get( metaKey ), String.class );
	}

	public List<String> getMetaList( String metaKey ) throws JsonSyntaxException
	{
		Type listType = new TypeToken<List<String>>()
		{
		}.getType();
		return GsonHolder.getInstance().fromJson( meta.get( metaKey ), listType );
	}

	/**
	 * Convenience method to compare values from meta with other ones.
	 *
	 * @param metaKey      The key of meta value (e.g. "title" or "thumbnail")
	 * @param compareValue A value to compare the concerning meta value with.
	 * @return true, if meta value exists and equals {@param compareValue}
	 */
	public boolean metaEquals( String metaKey, String compareValue )
	{
		if ( meta == null || !meta.containsKey( metaKey ) )
		{
			return false;
		}

		try
		{
			return getMetaString( metaKey ).equals( compareValue );
		}
		catch ( JsonSyntaxException e )
		{
			throw new JsonSyntaxException( "You tried to compare " + compareValue + " with a meta entry " + meta.get( metaKey ) + " which could not be parsed to a String.", e );
		}
	}

	/**
	 * the page's layout
	 */
	public String layout;

	@Override
	public String toString()
	{
		return "PagePreview [identifier = " + identifier + ", parent = " + parent + ", last_changed = " + last_changed + ", layout = " + layout + "]";
	}
}
