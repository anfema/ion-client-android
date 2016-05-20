package com.anfema.ionclient.pages.models;

import android.support.annotation.NonNull;

import com.anfema.ionclient.pages.models.contents.Connection;
import com.anfema.ionclient.pages.models.contents.ConnectionContent;
import com.anfema.ionclient.pages.models.contents.Content;
import com.anfema.ionclient.pages.models.contents.DatetimeContent;
import com.anfema.ionclient.pages.models.contents.Downloadable;
import com.anfema.ionclient.pages.models.contents.TextContent;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * A page holds all the information about its content.
 */
public class Page implements SizeAware
{
	/**
	 * Page identifier – matches the page_identifier header of the pages requests.
	 */
	public String identifier;

	/**
	 * identifier of the collection the page belongs to
	 */
	public String collection;

	/**
	 * date when published page has been updated the last time
	 */
	public DateTime last_changed;

	public String layout;

	/**
	 * e.g. "de_DE"
	 */
	public String locale;

	/**
	 * page identifier of the parent's page
	 */
	public String parent;

	/**
	 * page identifiers of sub-pages
	 */
	public List<String> children;

	public List<Content> contents;

	/**
	 * Generic content access
	 *
	 * @param outlet outlet identifier to identify content
	 * @return content or null if not available
	 */
	public Content getContent( String outlet )
	{
		if ( outlet == null )
		{
			return null;
		}

		for ( Content content : contents )
		{
			if ( outlet.equals( content.outlet ) )
			{
				return content;
			}
		}
		return null;
	}

	/**
	 * Type-agnostic content access
	 *
	 * @param contentSubclass the type of the requested content, it must be a subclass of {@link Content}
	 * @param outlet          outlet identifier to identify content
	 * @return content or null if not available
	 */
	public <T extends Content> T getContent( String outlet, Class<T> contentSubclass )
	{
		Content content = getContent( outlet );
		try
		{
			return contentSubclass.cast( content );
		}
		catch ( ClassCastException e )
		{
			return null;
		}
	}

	/**
	 * Filter contents by outlet identifier (for array contents).
	 *
	 * @param outlet outlet identifier to identify array of contents
	 */
	public List<Content> getContents( String outlet )
	{
		List<Content> contents = new ArrayList<>();
		if ( outlet != null )
		{
			for ( Content content : this.contents )
			{
				if ( outlet.equals( content.outlet ) )
				{
					contents.add( content );
				}
			}
		}
		return contents;
	}

	/**
	 * Filter contents by outlet identifier and type (for array contents).
	 *
	 * @param contentSubclass the type of the requested contents, it must be a subclass of {@link Content}
	 * @param outlet          outlet identifier to identify array of contents
	 */
	public <T extends Content> List<T> getContents( String outlet, Class<T> contentSubclass )
	{
		List<T> contents = new ArrayList<>();
		if ( outlet != null )
		{
			for ( Content content : this.contents )
			{
				if ( outlet.equals( content.outlet ) )
				{
					try
					{
						T castContent = contentSubclass.cast( content );
						contents.add( castContent );
					}
					catch ( ClassCastException ignored )
					{
					}
				}
			}
		}
		return contents;
	}

	/**
	 * Convenience method to obtain the formatted text of a {@link com.anfema.ionclient.pages.models.contents.TextContent}.
	 * <p>
	 * This method considers the mime-type and formats the text respectively.
	 *
	 * @param outlet outlet identifier to identify content
	 * @return formatted text if exists, {@code null} otherwise
	 */
	public CharSequence getTextOrNull( String outlet )
	{
		TextContent content = getContent( outlet, TextContent.class );
		return content != null ? content.getTextFormatted() : null;
	}

	/**
	 * Convenience method to obtain the formatted text of a {@link com.anfema.ionclient.pages.models.contents.TextContent}.
	 * <p>
	 * This method considers the mime-type and formats the text respectively.
	 *
	 * @param outlet outlet identifier to identify content
	 * @return formatted text if exists, empty string otherwise
	 */
	@NonNull
	public CharSequence getTextOrEmpty( String outlet )
	{
		CharSequence text = getTextOrNull( outlet );
		return text != null ? text : "";
	}

	/**
	 * Convenience method to obtain the text of a {@link com.anfema.ionclient.pages.models.contents.TextContent}.
	 * <p>
	 * Independent of actual mime-type, formatting tags are removed.
	 *
	 * @param outlet outlet identifier to identify content
	 * @return text without formatting if exists, {@code null} otherwise
	 */
	public String getPlainTextOrNull( String outlet )
	{
		CharSequence text = getTextOrNull( outlet );
		return text != null ? text.toString() : null;
	}

	/**
	 * Convenience method to obtain the text of a {@link com.anfema.ionclient.pages.models.contents.TextContent}.
	 * <p>
	 * Independent of actual mime-type, formatting tags are removed.
	 *
	 * @param outlet outlet identifier to identify content
	 * @return text without formatting if exists, empty string otherwise
	 */
	@NonNull
	public String getPlainTextOrEmpty( String outlet )
	{
		return getTextOrEmpty( outlet ).toString();
	}

	/**
	 * Parse connection from string
	 *
	 * @param outlet outlet identifier to identify content
	 * @return @return link to another collection, page, content if data exists, {@code null} otherwise
	 */
	public DateTime getDateTimeOrNull( String outlet )
	{
		DatetimeContent content = getContent( outlet, DatetimeContent.class );
		return content != null ? content.datetime : null;
	}

	/**
	 * Parse connection from string
	 *
	 * @param outlet outlet identifier to identify content
	 * @return @return link to another collection, page, content if data exists, {@code null} otherwise
	 */
	public Connection getConnectionOrNull( String outlet )
	{
		ConnectionContent content = getContent( outlet, ConnectionContent.class );
		return content != null ? content.connection : null;
	}

	/**
	 * Get downloadable content
	 *
	 * @param outlet outlet identifier to identify content
	 * @return content if it is a {@link Downloadable} content, or {@code null} otherwise
	 */
	public Downloadable getDownloadableOrNull( String outlet )
	{
		Content content = getContent( outlet );
		return content instanceof Downloadable ? ( Downloadable ) content : null;
	}

	public long byteCount;

	@Override
	public long byteCont()
	{
		return byteCount;
	}

	@Override
	public String toString()
	{
		return "Page [identifier = " + identifier + ", collection = " + collection + ", last_changed = " + last_changed + ", parent = " + parent + ", children = " + children + "]";
	}
}
