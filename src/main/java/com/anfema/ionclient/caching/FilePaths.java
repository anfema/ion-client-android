package com.anfema.ionclient.caching;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.exceptions.NoIonPagesRequestException;
import com.anfema.ionclient.pages.IonPageUrls;
import com.anfema.ionclient.pages.IonPageUrls.IonRequestInfo;
import com.anfema.ionclient.utils.FileUtils;
import com.anfema.utils.HashUtils;

import java.io.File;

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
	public static File getFilePath( String url, IonConfig config, Context context ) throws NoIonPagesRequestException
	{
		return getFilePath( url, config, context, false );
	}

	public static File getFilePath( String url, IonConfig config, Context context, boolean tempFolder ) throws NoIonPagesRequestException
	{
		IonRequestInfo requestInfo = IonPageUrls.analyze( url, config );

		switch ( requestInfo.requestType )
		{
			case COLLECTION:
				return getCollectionJsonPath( url, config, context, tempFolder );
			case PAGE:
				return getPageJsonPath( url, requestInfo.pageIdentifier, config, context, tempFolder );
			case ARCHIVE:
				return getArchiveFilePath( config, context );
			case MEDIA:
			default:
				return getMediaFilePath( url, config, context, tempFolder );
		}
	}

	/**
	 * Find appropriate file path for collection.
	 * <p>
	 * Creates folders if they do not exist yet.
	 */
	public static File getCollectionJsonPath( String url, IonConfig config, Context context )
	{
		return getCollectionJsonPath( url, config, context, false );
	}

	/**
	 * Find appropriate file path for collection.
	 * <p>
	 * Creates folders if they do not exist yet.
	 */
	public static File getCollectionJsonPath( String url, IonConfig config, Context context, boolean tempFolder )
	{
		if ( url == null || url.isEmpty() )
		{
			url = IonPageUrls.getCollectionUrl( config );
		}
		return new File( getCollectionFolderPath( config, context ).getPath() + appendTemp( tempFolder ), getFileName( url ) );
	}

	/**
	 * Find appropriate file path for page.
	 * <p>
	 * Creates folders if they do not exist yet.
	 */
	public static File getPageJsonPath( String url, String pageIdentifier, IonConfig config, Context context )
	{
		return getPageJsonPath( url, pageIdentifier, config, context, false );
	}

	/**
	 * Find appropriate file path for page.
	 * <p>
	 * Creates folders if they do not exist yet.
	 */
	public static File getPageJsonPath( String url, String pageIdentifier, IonConfig config, Context context, boolean tempFolder )
	{
		if ( url == null || url.isEmpty() )
		{
			url = IonPageUrls.getPageUrl( config, pageIdentifier );
		}
		return new File( getCollectionFolderPath( config, context ).getPath() + appendTemp( tempFolder ), getFileName( url ) );
	}

	/**
	 * Find appropriate file path for media files.
	 * <p>
	 * Creates folders if the do not exist yet.
	 */
	public static File getMediaFilePath( String url, IonConfig config, Context context )
	{
		return getMediaFilePath( url, config, context, false );
	}

	/**
	 * Find appropriate file path for media files.
	 * <p>
	 * Creates folders if the do not exist yet.
	 */
	public static File getMediaFilePath( String url, IonConfig config, Context context, boolean tempCollectionFolder )
	{
		File mediaFolderPath = getMediaFolderPath( config, context, tempCollectionFolder );
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
	public static File getMediaFolderPath( IonConfig config, Context context, boolean tempCollectionFolder )
	{
		return new File( getBasicCollectionFilePath( config, context ) + FileUtils.SLASH + MEDIA + appendTemp( tempCollectionFolder ) );
	}

	/**
	 * creates directories
	 */
	public static File getCollectionFolderPath( IonConfig config, Context context )
	{
		File folder = new File( getBasicCollectionFilePath( config, context ) + FileUtils.SLASH + config.locale + FileUtils.SLASH + config.variation );
		if ( !folder.exists() )
		{
			folder.mkdirs();
		}
		return folder;
	}

	public static File getArchiveFilePath( IonConfig config, Context context )
	{
		return new File( getBasicCollectionFilePath( config, context ) + FileUtils.SLASH + config.locale + FileUtils.SLASH + config.variation + ".archive" );
	}

	@NonNull
	private static String getBasicCollectionFilePath( IonConfig config, Context context )
	{
		return getBasicCollectionFilePath( config.collectionIdentifier, context );
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
