/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anfema.ionclient.utils;

/**
 * Represents an <a href="http://en.wikipedia.org/wiki/Internet_media_type">Internet Media Type</a>
 * (also known as a MIME Type or Content Type). This class also supports the concept of media ranges
 * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1">defined by HTTP/1.1</a>.
 * As such, the {@code *} character is treated as a wildcard and is used to represent any acceptable
 * type or subtype value. A media type may not have wildcard type with a declared subtype. The
 * {@code *} character has no special meaning as part of a parameter. All values for type, subtype,
 * parameter attributes or parameter values must be valid according to RFCs
 * <a href="http://www.ietf.org/rfc/rfc2045.txt">2045</a> and
 * <a href="http://www.ietf.org/rfc/rfc2046.txt">2046</a>.
 * <p>
 * <p>All portions of the media type that are case-insensitive (type, subtype, parameter attributes)
 * are normalized to lowercase. The value of the {@code charset} parameter is normalized to
 * lowercase, but all others are left as-is.
 * <p>
 * <p>Note that this specifically does <strong>not</strong> represent the value of the MIME
 * {@code Content-Type} header and as such has no support for header-specific considerations such as
 * line folding and comments.
 * <p>
 * <p>For media types that take a charset the predefined constants default to UTF-8 and have a
 * "_UTF_8" suffix. To get a version without a character set, use {@link #withoutParameters}.
 *
 * @author Gregory Kick
 * @since 12.0
 * <p>
 * File was altered, e.g. parameters were removed.
 */
public final class MediaType
{
	private static final String CHARSET_ATTRIBUTE = "charset";

	// TODO(gak): make these public?
	private static final String APPLICATION_TYPE = "application";
	private static final String AUDIO_TYPE       = "audio";
	private static final String IMAGE_TYPE       = "image";
	private static final String TEXT_TYPE        = "text";
	private static final String VIDEO_TYPE       = "video";

	private static final String WILDCARD = "*";

  /*
   * The following constants are grouped by their type and ordered alphabetically by the constant
   * name within that type. The constant name should be a sensible identifier that is closest to the
   * "common name" of the media.  This is often, but not necessarily the same as the subtype.
   *
   * Be sure to declare all constants with the type and subtype in all lowercase.
   *
   * When adding constants, be sure to add an entry into the KNOWN_TYPES map. For types that
   * take a charset (e.g. all text/* types), default to UTF-8 and suffix with "_UTF_8".
   */

	public static final MediaType ANY_TYPE             = createConstant( WILDCARD, WILDCARD );
	public static final MediaType ANY_TEXT_TYPE        = createConstant( TEXT_TYPE, WILDCARD );
	public static final MediaType ANY_IMAGE_TYPE       = createConstant( IMAGE_TYPE, WILDCARD );
	public static final MediaType ANY_AUDIO_TYPE       = createConstant( AUDIO_TYPE, WILDCARD );
	public static final MediaType ANY_VIDEO_TYPE       = createConstant( VIDEO_TYPE, WILDCARD );
	public static final MediaType ANY_APPLICATION_TYPE = createConstant( APPLICATION_TYPE, WILDCARD );

	/* text types */
	public static final MediaType CACHE_MANIFEST_UTF_8  =
			createConstantUtf8( TEXT_TYPE, "cache-manifest" );
	public static final MediaType CSS_UTF_8             = createConstantUtf8( TEXT_TYPE, "css" );
	public static final MediaType CSV_UTF_8             = createConstantUtf8( TEXT_TYPE, "csv" );
	public static final MediaType HTML_UTF_8            = createConstantUtf8( TEXT_TYPE, "html" );
	public static final MediaType I_CALENDAR_UTF_8      = createConstantUtf8( TEXT_TYPE, "calendar" );
	public static final MediaType PLAIN_TEXT_UTF_8      = createConstantUtf8( TEXT_TYPE, "plain" );
	/**
	 * <a href="http://www.rfc-editor.org/rfc/rfc4329.txt">RFC 4329</a> declares
	 * {@link #JAVASCRIPT_UTF_8 application/javascript} to be the correct media type for JavaScript,
	 * but this may be necessary in certain situations for compatibility.
	 */
	public static final MediaType TEXT_JAVASCRIPT_UTF_8 = createConstantUtf8( TEXT_TYPE, "javascript" );
	public static final MediaType VCARD_UTF_8           = createConstantUtf8( TEXT_TYPE, "vcard" );
	public static final MediaType WML_UTF_8             = createConstantUtf8( TEXT_TYPE, "vnd.wap.wml" );
	/**
	 * As described in <a href="http://www.ietf.org/rfc/rfc3023.txt">RFC 3023</a>, this constant
	 * ({@code text/xml}) is used for XML documents that are "readable by casual users."
	 * {@link #APPLICATION_XML_UTF_8} is provided for documents that are intended for applications.
	 */
	public static final MediaType XML_UTF_8             = createConstantUtf8( TEXT_TYPE, "xml" );

	/* image types */
	public static final MediaType BMP       = createConstant( IMAGE_TYPE, "bmp" );
	public static final MediaType GIF       = createConstant( IMAGE_TYPE, "gif" );
	public static final MediaType ICO       = createConstant( IMAGE_TYPE, "vnd.microsoft.icon" );
	public static final MediaType JPEG      = createConstant( IMAGE_TYPE, "jpeg" );
	public static final MediaType PNG       = createConstant( IMAGE_TYPE, "png" );
	public static final MediaType SVG_UTF_8 = createConstantUtf8( IMAGE_TYPE, "svg+xml" );
	public static final MediaType TIFF      = createConstant( IMAGE_TYPE, "tiff" );
	public static final MediaType WEBP      = createConstant( IMAGE_TYPE, "webp" );

	/* audio types */
	public static final MediaType MP4_AUDIO  = createConstant( AUDIO_TYPE, "mp4" );
	public static final MediaType MPEG_AUDIO = createConstant( AUDIO_TYPE, "mpeg" );
	public static final MediaType OGG_AUDIO  = createConstant( AUDIO_TYPE, "ogg" );
	public static final MediaType WEBM_AUDIO = createConstant( AUDIO_TYPE, "webm" );

	/* video types */
	public static final MediaType MP4_VIDEO  = createConstant( VIDEO_TYPE, "mp4" );
	public static final MediaType MPEG_VIDEO = createConstant( VIDEO_TYPE, "mpeg" );
	public static final MediaType OGG_VIDEO  = createConstant( VIDEO_TYPE, "ogg" );
	public static final MediaType QUICKTIME  = createConstant( VIDEO_TYPE, "quicktime" );
	public static final MediaType WEBM_VIDEO = createConstant( VIDEO_TYPE, "webm" );
	public static final MediaType WMV        = createConstant( VIDEO_TYPE, "x-ms-wmv" );

  	/* application types */
	/**
	 * As described in <a href="http://www.ietf.org/rfc/rfc3023.txt">RFC 3023</a>, this constant
	 * ({@code application/xml}) is used for XML documents that are "unreadable by casual users."
	 * {@link #XML_UTF_8} is provided for documents that may be read by users.
	 */
	public static final MediaType APPLICATION_XML_UTF_8     = createConstantUtf8( APPLICATION_TYPE, "xml" );
	public static final MediaType ATOM_UTF_8                = createConstantUtf8( APPLICATION_TYPE, "atom+xml" );
	public static final MediaType BZIP2                     = createConstant( APPLICATION_TYPE, "x-bzip2" );
	public static final MediaType FORM_DATA                 = createConstant( APPLICATION_TYPE, "x-www-form-urlencoded" );
	/**
	 * This is a non-standard media type, but is commonly used in serving hosted binary files as it is
	 * <a href="http://code.google.com/p/browsersec/wiki/Part2#Survey_of_content_sniffing_behaviors">
	 * known not to trigger content sniffing in current browsers</a>. It <i>should not</i> be used in
	 * other situations as it is not specified by any RFC and does not appear in the <a href=
	 * "http://www.iana.org/assignments/media-types">/IANA MIME Media Types</a> list. Consider
	 * {@link #OCTET_STREAM} for binary data that is not being served to a browser.
	 *
	 * @since 14.0
	 */
	public static final MediaType APPLICATION_BINARY        = createConstant( APPLICATION_TYPE, "binary" );
	public static final MediaType GZIP                      = createConstant( APPLICATION_TYPE, "x-gzip" );
	/**
	 * <a href="http://www.rfc-editor.org/rfc/rfc4329.txt">RFC 4329</a> declares this to be the
	 * correct media type for JavaScript, but {@link #TEXT_JAVASCRIPT_UTF_8 text/javascript} may be
	 * necessary in certain situations for compatibility.
	 */
	public static final MediaType JAVASCRIPT_UTF_8          =
			createConstantUtf8( APPLICATION_TYPE, "javascript" );
	public static final MediaType JSON_UTF_8                = createConstantUtf8( APPLICATION_TYPE, "json" );
	public static final MediaType KML                       = createConstant( APPLICATION_TYPE, "vnd.google-earth.kml+xml" );
	public static final MediaType KMZ                       = createConstant( APPLICATION_TYPE, "vnd.google-earth.kmz" );
	public static final MediaType MBOX                      = createConstant( APPLICATION_TYPE, "mbox" );
	public static final MediaType MICROSOFT_EXCEL           = createConstant( APPLICATION_TYPE, "vnd.ms-excel" );
	public static final MediaType MICROSOFT_POWERPOINT      =
			createConstant( APPLICATION_TYPE, "vnd.ms-powerpoint" );
	public static final MediaType MICROSOFT_WORD            = createConstant( APPLICATION_TYPE, "msword" );
	public static final MediaType OCTET_STREAM              = createConstant( APPLICATION_TYPE, "octet-stream" );
	public static final MediaType OGG_CONTAINER             = createConstant( APPLICATION_TYPE, "ogg" );
	public static final MediaType OOXML_DOCUMENT            = createConstant( APPLICATION_TYPE,
			"vnd.openxmlformats-officedocument.wordprocessingml.document" );
	public static final MediaType OOXML_PRESENTATION        = createConstant( APPLICATION_TYPE,
			"vnd.openxmlformats-officedocument.presentationml.presentation" );
	public static final MediaType OOXML_SHEET               =
			createConstant( APPLICATION_TYPE, "vnd.openxmlformats-officedocument.spreadsheetml.sheet" );
	public static final MediaType OPENDOCUMENT_GRAPHICS     =
			createConstant( APPLICATION_TYPE, "vnd.oasis.opendocument.graphics" );
	public static final MediaType OPENDOCUMENT_PRESENTATION =
			createConstant( APPLICATION_TYPE, "vnd.oasis.opendocument.presentation" );
	public static final MediaType OPENDOCUMENT_SPREADSHEET  =
			createConstant( APPLICATION_TYPE, "vnd.oasis.opendocument.spreadsheet" );
	public static final MediaType OPENDOCUMENT_TEXT         =
			createConstant( APPLICATION_TYPE, "vnd.oasis.opendocument.text" );
	public static final MediaType PDF                       = createConstant( APPLICATION_TYPE, "pdf" );
	public static final MediaType POSTSCRIPT                = createConstant( APPLICATION_TYPE, "postscript" );
	public static final MediaType RDF_XML_UTF_8             = createConstantUtf8( APPLICATION_TYPE, "rdf+xml" );
	public static final MediaType RTF_UTF_8                 = createConstantUtf8( APPLICATION_TYPE, "rtf" );
	public static final MediaType SHOCKWAVE_FLASH           = createConstant( APPLICATION_TYPE,
			"x-shockwave-flash" );
	public static final MediaType SKETCHUP                  = createConstant( APPLICATION_TYPE, "vnd.sketchup.skp" );
	public static final MediaType TAR                       = createConstant( APPLICATION_TYPE, "x-tar" );
	public static final MediaType XHTML_UTF_8               = createConstantUtf8( APPLICATION_TYPE, "xhtml+xml" );
	/**
	 * Media type for Extensible Resource Descriptors. This is not yet registered with the IANA, but
	 * it is specified by OASIS in the
	 * <a href="http://docs.oasis-open.org/xri/xrd/v1.0/cd02/xrd-1.0-cd02.html"> XRD definition</a>
	 * and implemented in projects such as
	 * <a href="http://code.google.com/p/webfinger/">WebFinger</a>.
	 */
	public static final MediaType XRD_UTF_8                 = createConstantUtf8( APPLICATION_TYPE, "xrd+xml" );
	public static final MediaType ZIP                       = createConstant( APPLICATION_TYPE, "zip" );

	private final String type;
	private final String subtype;

	private MediaType( String type, String subtype )
	{
		this.type = type;
		this.subtype = subtype;
	}

	private static MediaType createConstant( String type, String subtype )
	{
		return new MediaType( type, subtype );
	}

	private static MediaType createConstantUtf8( String type, String subtype )
	{
		return new MediaType( type, subtype );
	}

	/**
	 * Returns the top-level media type.  For example, {@code "text"} in {@code "text/plain"}.
	 */
	public String type()
	{
		return type;
	}

	/**
	 * Returns the media subtype.  For example, {@code "plain"} in {@code "text/plain"}.
	 */
	public String subtype()
	{
		return subtype;
	}

	/**
	 * Returns the media subtype.  For example, {@code "text/plain"} in {@code "text/plain"}.
	 */
	@Override
	public String toString()
	{
		return type + "/" + subtype();
	}

	/**
	 * Returns true if either the type or subtype is the wildcard.
	 */
	public boolean hasWildcard()
	{
		return WILDCARD.equals( type ) || WILDCARD.equals( subtype );
	}

	/**
	 * Creates a new media type with the given type and subtype.
	 *
	 * @throws IllegalArgumentException if type or subtype is invalid or if a wildcard is used for the
	 *                                  type, but not the subtype.
	 */
	public static MediaType create( String type, String subtype )
	{
		return create( type, subtype );
	}

	/**
	 * Creates a media type with the "application" type and the given subtype.
	 *
	 * @throws IllegalArgumentException if subtype is invalid
	 */
	static MediaType createApplicationType( String subtype )
	{
		return create( APPLICATION_TYPE, subtype );
	}

	/**
	 * Creates a media type with the "audio" type and the given subtype.
	 *
	 * @throws IllegalArgumentException if subtype is invalid
	 */
	static MediaType createAudioType( String subtype )
	{
		return create( AUDIO_TYPE, subtype );
	}

	/**
	 * Creates a media type with the "image" type and the given subtype.
	 *
	 * @throws IllegalArgumentException if subtype is invalid
	 */
	static MediaType createImageType( String subtype )
	{
		return create( IMAGE_TYPE, subtype );
	}

	/**
	 * Creates a media type with the "text" type and the given subtype.
	 *
	 * @throws IllegalArgumentException if subtype is invalid
	 */
	static MediaType createTextType( String subtype )
	{
		return create( TEXT_TYPE, subtype );
	}

	/**
	 * Creates a media type with the "video" type and the given subtype.
	 *
	 * @throws IllegalArgumentException if subtype is invalid
	 */
	static MediaType createVideoType( String subtype )
	{
		return create( VIDEO_TYPE, subtype );
	}

	private static String escapeAndQuote( String value )
	{
		StringBuilder escaped = new StringBuilder( value.length() + 16 ).append( '"' );
		for ( char ch : value.toCharArray() )
		{
			if ( ch == '\r' || ch == '\\' || ch == '"' )
			{
				escaped.append( '\\' );
			}
			escaped.append( ch );
		}
		return escaped.append( '"' ).toString();
	}
}
