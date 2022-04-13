package com.anfema.ionclient.caching

import android.content.Context
import com.anfema.ionclient.CollectionProperties
import com.anfema.ionclient.exceptions.NoIonPagesRequestException
import com.anfema.ionclient.pages.IonPageUrls
import com.anfema.ionclient.pages.IonPageUrls.IonRequestType
import com.anfema.ionclient.pages.IonPageUrls.getCollectionUrl
import com.anfema.ionclient.pages.IonPageUrls.getPageUrl
import com.anfema.ionclient.utils.FileUtils
import com.anfema.utils.HashUtils
import java.io.File

internal object FilePaths {
    private const val MEDIA = "media"
    private const val TEMP_EXT = "_temp"

    /**
     * Finds out if request is collection, page, or media request
     * and delegates to respective file path methods.
     *
     * @throws NoIonPagesRequestException
     */
    @Throws(NoIonPagesRequestException::class)
    fun getFilePath(url: String, collectionProperties: CollectionProperties, context: Context): File =
        getFilePath(url, collectionProperties, context, false)

    @Throws(NoIonPagesRequestException::class)
    fun getFilePath(
        url: String,
        collectionProperties: CollectionProperties,
        context: Context,
        tempFolder: Boolean,
    ): File {
        val requestInfo = IonPageUrls.analyze(url, collectionProperties.baseUrl)

        return when (requestInfo.requestType) {
            IonRequestType.COLLECTION -> getCollectionJsonPath(
                url = url,
                collectionProperties = collectionProperties,
                context = context,
                tempFolder = tempFolder
            )
            IonRequestType.PAGE -> getPageJsonPath(
                url = url,
                pageIdentifier = requestInfo.pageIdentifier,
                collectionProperties = collectionProperties,
                context = context,
                tempFolder = tempFolder
            )
            IonRequestType.ARCHIVE -> getArchiveFilePath(collectionProperties, context)
            IonRequestType.MEDIA -> getMediaFilePath(url, collectionProperties, context, tempFolder)
        }
    }

    /**
     * Find appropriate file path for collection.
     *
     *
     * Creates folders if they do not exist yet.
     */
    fun getCollectionJsonPath(url: String, collectionProperties: CollectionProperties, context: Context): File =
        getCollectionJsonPath(url, collectionProperties, context, false)

    /**
     * Find appropriate file path for collection.
     *
     *
     * Creates folders if they do not exist yet.
     */
    private fun getCollectionJsonPath(
        url: String,
        collectionProperties: CollectionProperties,
        context: Context,
        tempFolder: Boolean,
    ) = File(
        getCollectionFolderPath(collectionProperties, context).path + appendTemp(tempFolder),
        getFileName(url.ifEmpty { collectionProperties.getCollectionUrl() })
    )

    /**
     * Find appropriate file path for page.
     *
     *
     * Creates folders if they do not exist yet.
     */
    fun getPageJsonPath(
        url: String?,
        pageIdentifier: String?,
        collectionProperties: CollectionProperties,
        context: Context,
    ): File {
        return getPageJsonPath(url, pageIdentifier, collectionProperties, context, false)
    }

    /**
     * Find appropriate file path for page.
     *
     *
     * Creates folders if they do not exist yet.
     */
    fun getPageJsonPath(
        url: String?,
        pageIdentifier: String?,
        collectionProperties: CollectionProperties,
        context: Context,
        tempFolder: Boolean,
    ): File {
        var url = url
        if (url == null || url.isEmpty()) {
            url = collectionProperties.getPageUrl(pageIdentifier!!)
        }
        return File(getCollectionFolderPath(collectionProperties, context).path + appendTemp(tempFolder),
            getFileName(url))
    }

    /**
     * Find appropriate file path for media files.
     *
     *
     * Creates folders if the do not exist yet.
     */
    fun getMediaFilePath(url: String?, collectionProperties: CollectionProperties, context: Context): File {
        return getMediaFilePath(url, collectionProperties, context, false)
    }

    /**
     * Find appropriate file path for media files.
     *
     *
     * Creates folders if the do not exist yet.
     */
    fun getMediaFilePath(
        url: String?,
        collectionProperties: CollectionProperties,
        context: Context,
        tempCollectionFolder: Boolean,
    ): File {
        val mediaFolderPath = getMediaFolderPath(collectionProperties, context, tempCollectionFolder)
        if (!mediaFolderPath.exists()) {
            mediaFolderPath.mkdirs()
        }
        val filename = getFileName(url)
        return File(mediaFolderPath, filename)
    }

    /**
     * does not create directories
     */
    fun getMediaFolderPath(
        collectionProperties: CollectionProperties,
        context: Context,
        tempCollectionFolder: Boolean,
    ): File {
        return File(getBasicCollectionFilePath(collectionProperties.collectionIdentifier,
            context) + FileUtils.SLASH + MEDIA + appendTemp(tempCollectionFolder))
    }

    /**
     * creates directories
     */
    fun getCollectionFolderPath(collectionProperties: CollectionProperties, context: Context): File {
        val folder = File(getBasicCollectionFilePath(collectionProperties.collectionIdentifier,
            context) + FileUtils.SLASH + collectionProperties.locale + FileUtils.SLASH + collectionProperties.variation)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder
    }

    fun getArchiveFilePath(collectionProperties: CollectionProperties, context: Context): File {
        return File(getBasicCollectionFilePath(collectionProperties.collectionIdentifier,
            context) + FileUtils.SLASH + collectionProperties.locale + FileUtils.SLASH + collectionProperties.variation + ".archive")
    }

    private fun getBasicCollectionFilePath(collectionIdentifier: String, context: Context): String {
        return getFilesDir(context).toString() + FileUtils.SLASH + collectionIdentifier
    }

    fun getSharedPrefsFile(sharedPrefsName: String, context: Context): File {
        val appFolder = getFilesDir(context).parent
        return File("$appFolder/shared_prefs", "$sharedPrefsName.xml")
    }

    fun getFilesDir(context: Context): File {
        return context.filesDir
    }

    fun getFileName(url: String?): String {
        return HashUtils.getMD5Hash(url)
    }

    @JvmStatic
    fun getTempFilePath(file: File): File {
        val fileTemp = getTempName(file)
        FileUtils.createDir(file.parentFile)
        return fileTemp
    }

    private fun getTempName(file: File): File {
        val folderTemp = File(file.path + TEMP_EXT)
        FileUtils.reset(folderTemp)
        return folderTemp
    }

    private fun appendTemp(appendTemp: Boolean): String {
        return if (appendTemp) TEMP_EXT else ""
    }
}
