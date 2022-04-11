package com.anfema.ionclient.caching;

import android.content.Context;

import com.anfema.ionclient.CollectionProperties;
import com.anfema.ionclient.exceptions.NoIonPagesRequestException;
import com.anfema.ionclient.pages.IonPageUrls;
import com.anfema.ionclient.pages.IonPageUrls.IonRequestInfo;
import com.anfema.ionclient.utils.FileUtils;
import com.anfema.utils.HashUtils;

import java.io.File;

import androidx.annotation.NonNull;

public class FilePaths
{
	private static final String MEDIA    = "media";
	private static final String TEMP_EXT = "_temp";

	/**
	 * Finds out if request is collection, page, or media request
	 * and delegates to respective file path methods.
	 *
	 * @throws NoIonPagesRequestException
	 */
	@NonNull
	public static File getFilePath( String url, CollectionProperties collectionProperties, Context context ) throws NoIonPagesRequestException
	{
		return getFilePath( url, collectionProperties, context, false );
	}

	@NonNull
	public static File getFilePath( String url, CollectionProperties collectionProperties, Context context, boolean tempFolder ) throws NoIonPagesRequestException
	{
		IonRequestInfo requestInfo = IonPageUrls.analyze( url, collectionProperties );

		switch ( requestInfo.getRequestType() )
		{
			case COLLECTION:
				return getCollectionJsonPath( url, collectionProperties, context, tempFolder );
			case PAGE:
				return getPageJsonPath( url, requestInfo.getPageIdentifier(), collectionProperties, context, tempFolder );
			case ARCHIVE:
				return getArchiveFilePath( collectionProperties, context );
			case MEDIA:
			default:
				return getMediaFilePath( url, collectionProperties, context, tempFolder );
		}
	}

	/**
	 * Find appropriate file path for collection.
	 * <p>
	 * Creates folders if they do not exist yet.
	 */
	@NonNull
	public static File getCollectionJsonPath( String url, CollectionProperties collectionProperties, Context context )
	{
		return getCollectionJsonPath( url, collectionProperties, context, false );
	}

	/**
	 * Find appropriate file path for collection.
	 * <p>
	 * Creates folders if they do not exist yet.
	 */
	@NonNull
	public static File getCollectionJsonPath( String url, CollectionProperties collectionProperties, Context context, boolean tempFolder )
	{
		if ( url == null || url.isEmpty() )
		{
			url = IonPageUrls.getCollectionUrl( collectionProperties );
		}
		return new File( getCollectionFolderPath( collectionProperties, context ).getPath() + appendTemp( tempFolder ), getFileName( url ) );
	}

	/**
	 * Find appropriate file path for page.
	 * <p>
	 * Creates folders if they do not exist yet.
	 */
	@NonNull
	public static File getPageJsonPath( String url, String pageIdentifier, CollectionProperties collectionProperties, Context context )
	{
		return getPageJsonPath( url, pageIdentifier, collectionProperties, context, false );
	}

	/**
	 * Find appropriate file path for page.
	 * <p>
	 * Creates folders if they do not exist yet.
	 */
	@NonNull
	public static File getPageJsonPath( String url, String pageIdentifier, CollectionProperties collectionProperties, Context context, boolean tempFolder )
	{
		if ( url == null || url.isEmpty() )
		{
			url = IonPageUrls.getPageUrl( collectionProperties, pageIdentifier );
		}
		return new File( getCollectionFolderPath( collectionProperties, context ).getPath() + appendTemp( tempFolder ), getFileName( url ) );
	}

	/**
	 * Find appropriate file path for media files.
	 * <p>
	 * Creates folders if the do not exist yet.
	 */
	@NonNull
	public static File getMediaFilePath( String url, CollectionProperties collectionProperties, Context context )
	{
		return getMediaFilePath( url, collectionProperties, context, false );
	}

	/**
	 * Find appropriate file path for media files.
	 * <p>
	 * Creates folders if the do not exist yet.
	 */
	@NonNull
	public static File getMediaFilePath( String url, CollectionProperties collectionProperties, Context context, boolean tempCollectionFolder )
	{
		File mediaFolderPath = getMediaFolderPath( collectionProperties, context, tempCollectionFolder );
		if ( !mediaFolderPath.exists() )
		{
			mediaFolderPath.mkdirs();
		}
		String filename = getFileName( url );
		return new File( mediaFolderPath, filename );
	}

	/**
	 * does not create directories
	 */
	@NonNull
	public static File getMediaFolderPath( CollectionProperties collectionProperties, Context context, boolean tempCollectionFolder )
	{
		return new File( getBasicCollectionFilePath( collectionProperties.collectionIdentifier, context ) + FileUtils.SLASH + MEDIA + appendTemp( tempCollectionFolder ) );
	}

	/**
	 * creates directories
	 */
	@NonNull
	public static File getCollectionFolderPath( CollectionProperties collectionProperties, Context context )
	{
		File folder = new File( getBasicCollectionFilePath( collectionProperties.collectionIdentifier, context ) + FileUtils.SLASH + collectionProperties.locale + FileUtils.SLASH + collectionProperties.variation );
		if ( !folder.exists() )
		{
			folder.mkdirs();
		}
		return folder;
	}

	@NonNull
	public static File getArchiveFilePath( CollectionProperties collectionProperties, Context context )
	{
		return new File( getBasicCollectionFilePath( collectionProperties.collectionIdentifier, context ) + FileUtils.SLASH + collectionProperties.locale + FileUtils.SLASH + collectionProperties.variation + ".archive" );
	}

	@NonNull
	private static String getBasicCollectionFilePath( String collectionIdentifier, Context context )
	{
		return getFilesDir( context ) + FileUtils.SLASH + collectionIdentifier;
	}

	@NonNull
	public static File getSharedPrefsFile( String sharedPrefsName, Context context )
	{
		String appFolder = FilePaths.getFilesDir( context ).getParent();
		return new File( appFolder + "/shared_prefs", sharedPrefsName + ".xml" );
	}

	public static File getFilesDir( Context context )
	{
		return context.getFilesDir();
	}

	@NonNull
	public static String getFileName( String url )
	{
		return HashUtils.getMD5Hash( url );
	}

	@NonNull
	public static File getTempFilePath( File file )
	{
		File fileTemp = getTempName( file );
		FileUtils.createDir( file.getParentFile() );
		return fileTemp;
	}

	@NonNull
	private static File getTempName( File file )
	{
		File folderTemp = new File( file.getPath() + TEMP_EXT );
		FileUtils.reset( folderTemp );
		return folderTemp;
	}

	private static String appendTemp( boolean appendTemp )
	{
		return appendTemp ? TEMP_EXT : "";
	}
}
