package com.anfema.ionclient.archive

import android.content.Context
import com.anfema.ionclient.CollectionProperties
import com.anfema.ionclient.archive.models.ArchiveIndex
import com.anfema.ionclient.caching.FilePaths
import com.anfema.ionclient.caching.MemoryCache
import com.anfema.ionclient.caching.index.CacheIndexStore
import com.anfema.ionclient.caching.index.CollectionCacheIndex
import com.anfema.ionclient.caching.index.FileCacheIndex
import com.anfema.ionclient.caching.index.PageCacheIndex
import com.anfema.ionclient.exceptions.FileMoveException
import com.anfema.ionclient.exceptions.NoIonPagesRequestException
import com.anfema.ionclient.exceptions.PageNotInCollectionException
import com.anfema.ionclient.pages.IonPageUrls
import com.anfema.ionclient.pages.IonPageUrls.IonRequestType
import com.anfema.ionclient.pages.IonPageUrls.getCollectionUrl
import com.anfema.ionclient.pages.models.Collection
import com.anfema.ionclient.pages.models.responses.CollectionResponse
import com.anfema.ionclient.serialization.GsonHolder
import com.anfema.ionclient.utils.FileUtils
import com.anfema.ionclient.utils.IonLog
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.apache.commons.compress.archivers.ArchiveException
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.joda.time.DateTime
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.LinkedList

internal object ArchiveUtils {
    private const val TAG = "ArchiveUtils"

    fun unTar(
        archiveFile: File,
        collection: Collection,
        lastModified: String?,
        requestTime: DateTime,
        collectionProperties: CollectionProperties,
        context: Context,
    ): Completable {

        // if lastModified date was not passed, look if cache index entry exists for collection and retrieve it from there
        val recoveredLastModified = lastModified ?: getCollectionLastModifiedFromCache(collectionProperties, context)

        return Single.fromCallable {
            performUnTar(
                archiveFile = archiveFile,
                collectionProperties = collectionProperties,
                collection = collection,
                inLastModified = recoveredLastModified,
                requestTime = requestTime,
                context = context,
            )
        }
            .flatMapObservable { source: List<FileWithMeta>? -> Observable.fromIterable(source) }
            // write cache index entries
            .map { fileWithType: FileWithMeta ->
                saveCacheIndex(
                    fileWithMeta = fileWithType,
                    collection = collection,
                    lastModified = recoveredLastModified,
                    requestTime = requestTime,
                    collectionProperties = collectionProperties,
                    context = context,
                )
            }
            .ignoreElements()
    }

    private fun getCollectionLastModifiedFromCache(
        collectionProperties: CollectionProperties,
        context: Context,
    ): String? =
        CollectionCacheIndex.retrieve(collectionProperties, context)?.lastModified
            ?.also { IonLog.d(TAG, "Restoring last_modified from cache index: $it") }

    /**
     * Untar an input file into an output file.
     *
     *
     * The output file is created in the output folder, having the same name
     * as the input file, minus the '.tar' extension.
     *
     * @param archiveFile  input TAR file
     * @param inLastModified when collection has been last modified
     * @param requestTime  the time, when the archive download was initiated
     * @throws IOException
     * @throws ArchiveException
     */
    @Throws(IOException::class, ArchiveException::class)
    private fun performUnTar(
        archiveFile: File,
        collectionProperties: CollectionProperties,
        collection: Collection,
        inLastModified: String?,
        requestTime: DateTime,
        context: Context,
    ): List<FileWithMeta> {

        val lastModified = inLastModified
            ?: getCollectionLastModifiedFromCache(collectionProperties, context)

        val collectionFolder = FilePaths.getCollectionFolderPath(collectionProperties, context)

        val untaredFiles: MutableList<FileWithMeta> = LinkedList()
        var archiveIndexes: List<ArchiveIndex> = emptyList()

        IonLog.d(TAG, String.format("Untaring %s to dir %s.", archiveFile.path, collectionFolder.path))

        FileInputStream(archiveFile)
            .use { inputStream ->
                (ArchiveStreamFactory().createArchiveInputStream("tar", inputStream) as TarArchiveInputStream)
                    .use { debInputStream ->

                        var indexHasBeenRead = false
                        while (true) {
                            val entry: TarArchiveEntry = debInputStream.nextEntry as? TarArchiveEntry ?: break

                            if (!indexHasBeenRead) {
                                // get index.json
                                val inputStreamReader = InputStreamReader(debInputStream, "UTF-8")
                                archiveIndexes =
                                    listOf(*GsonHolder.defaultInstance.fromJson(inputStreamReader,
                                        Array<ArchiveIndex>::class.java))
                                indexHasBeenRead = true
                                continue
                            }

                            // write the "content" files
                            if (!entry.isDirectory) {
                                val archiveFileName = entry.name
                                val fileInfo = ArchiveIndex.getByName(archiveFileName, archiveIndexes)
                                if (fileInfo == null) {
                                    IonLog.w(TAG, "Skipping " + entry.name + " because it was not found in index.json.")
                                    continue
                                }
                                IonLog.i(TAG, fileInfo.url)
                                val fileWithMeta =
                                    getFilePath(fileInfo, collectionFolder, collectionProperties, context)
                                var targetFile = fileWithMeta.fileTemp
                                FileUtils.createDir(targetFile!!.parentFile)
                                targetFile = FileUtils.writeToFile(debInputStream, targetFile)
                                if (targetFile != null) {
                                    untaredFiles.add(fileWithMeta)
                                }
                            }
                        }
                        IonLog.d(TAG, "Number of index entries found in archive: " + archiveIndexes.size)
                        IonLog.d(TAG, "Number of files found in archive: " + untaredFiles.size)
                    }
            }

        // clear memory cache because there is no way to selectively delete entries
        MemoryCache.clear()

        // merge files from archive download into collection's cache
        for (fileWithMeta in untaredFiles) {
            try {
                val writeSuccess = FileUtils.move(fileWithMeta.fileTemp, fileWithMeta.file, true)
                if (!writeSuccess) {
                    throw IOException("File could not be moved to its final path '" + fileWithMeta.file.path + "'")
                }
            } catch (e: FileMoveException) {
                var entriesWithSameIdentifier = 0
                for (meta in untaredFiles) {
                    if (fileWithMeta.pageIdentifier != null && fileWithMeta.pageIdentifier == meta.pageIdentifier) {
                        entriesWithSameIdentifier += 1
                    }
                }
                if (fileWithMeta.file.exists() && entriesWithSameIdentifier > 1) {
                    IonLog.w("FileMoveException", "URL: " + fileWithMeta.originUrl
                        + ", ignore it because it was probably caused by the file being duplicated in the TAR file."
                    )
                } else {
                    IonLog.e("FileMoveException", "URL: " + fileWithMeta.originUrl)
                    throw e
                }
            }
        }

        // remove old pages/media files incl. cache entries (those which are not listed in index json)
        val archiveUrls: Set<String> = archiveIndexes.map { it.url }.toSet()
        // get all index entries stored in the cache index store
        val allIndexes = CacheIndexStore.retrieveAllUrls(collectionProperties, context)
        // subtract the current index entries - leaving outdated cache index entries
        val outdatedUrls = allIndexes - archiveUrls

        for (outdatedUrl in outdatedUrls) {
            CacheIndexStore.delete(outdatedUrl, collectionProperties, context)
            try {
                // delete all files but archive file
                val file = FilePaths.getFilePath(outdatedUrl, collectionProperties, context)
                if (file.exists() && file.path != archiveFile.path) {
                    file.delete()
                }
            } catch (e: NoIonPagesRequestException) {
                IonLog.e(TAG, "Tried to delete file for URL $outdatedUrl\nBut the URL is not a valid ION Request URL")
            }
        }

        // add collection to file cache again
        MemoryCache.saveCollection(collection, collectionProperties, context)
        try {
            saveCollectionToFileCache(collectionProperties, collection, context)
            CollectionCacheIndex.save(collectionProperties, context, lastModified, requestTime)
        } catch (e: IOException) {
            IonLog.e("ION Archive", "Collection could not be saved.")
            IonLog.ex(e)
        }

        // add archive to file cache again - not the actual file, but the last updated information is required for subsequent archive downloads
        if (archiveFile.exists() && collection.archive != null) {
            FileCacheIndex.save(collection.archive, archiveFile, collectionProperties, null, requestTime, context)
            // delete archiveFile - yes that introduces an inconsistency, but it saves storage space on the other side
            archiveFile.delete()
        } else {
            val archiveFilePath = archiveFile.path
            IonLog.e(
                TAG,
                "Archive Index entry could not be saved. Archive file path: $archiveFilePath, Collection: $collection"
            )
        }

        // cache index entries are not written yet at this point
        return untaredFiles
    }

    @Throws(IOException::class)
    private fun saveCollectionToFileCache(
        collectionProperties: CollectionProperties,
        collection: Collection,
        context: Context,
    ) {
        val collectionUrl = collectionProperties.getCollectionUrl()
        val filePath = FilePaths.getCollectionJsonPath(collectionUrl, collectionProperties, context)
        val collectionJson = GsonHolder.defaultInstance.toJson(CollectionResponse(collection))
        FileUtils.writeTextToFile(collectionJson, filePath)
    }

    private fun getFilePath(
        fileInfo: ArchiveIndex,
        collectionFolder: File,
        collectionProperties: CollectionProperties,
        context: Context,
    ): FileWithMeta {
        var targetFileTemp: File?
        var targetFile: File
        var type: IonRequestType? = null
        val url = fileInfo.url
        var pageIdentifier: String? = null
        val filename = FilePaths.getFileName(url)
        try {
            // check URL is a collections or pages call
            val requestInfo = IonPageUrls.analyze(url, collectionProperties.baseUrl)
            pageIdentifier = requestInfo.pageIdentifier
            type = requestInfo.requestType
            targetFileTemp = FilePaths.getFilePath(url, collectionProperties, context, true)
            targetFile = FilePaths.getFilePath(url, collectionProperties, context, false)
        } catch (e: NoIonPagesRequestException) {
            IonLog.w(TAG, "URL $url cannot be handled properly. Is it invalid?")
            val collectionFolderTemp = FilePaths.getTempFilePath(collectionFolder)
            targetFileTemp = File(collectionFolderTemp, filename)
            targetFile = File(collectionFolder, filename)
        }
        return FileWithMeta(targetFile, targetFileTemp, type, url, fileInfo, pageIdentifier)
    }

    private fun saveCacheIndex(
        fileWithMeta: FileWithMeta,
        collection: Collection,
        lastModified: String?,
        requestTime: DateTime,
        collectionProperties: CollectionProperties,
        context: Context,
    ): File {
        val type = fileWithMeta.type
        if (type == null) {
            IonLog.w(
                TAG,
                "It could not be determined of which kind the request " + fileWithMeta.archiveIndex.url + " is. Thus, do not create a cache index entry."
            )
            return fileWithMeta.file
        }
        when (type) {
            IonRequestType.COLLECTION -> CollectionCacheIndex.save(
                collectionProperties = collectionProperties,
                context = context,
                lastModified = lastModified,
                lastUpdated = requestTime,
            )
            IonRequestType.PAGE -> {
                val pageIdentifier = fileWithMeta.pageIdentifier!!
                val lastChanged: DateTime? = try {
                    collection.getPageLastChanged(pageIdentifier)
                } catch (e: PageNotInCollectionException) {
                    IonLog.ex(TAG, e)
                    null
                }
                PageCacheIndex.save(pageIdentifier, lastChanged!!, collectionProperties, context)
            }
            IonRequestType.MEDIA -> FileCacheIndex.save(
                requestUrl = fileWithMeta.archiveIndex.url,
                file = fileWithMeta.file,
                collectionProperties = collectionProperties,
                checksum = fileWithMeta.archiveIndex.checksum,
                requestTime = requestTime,
                context = context,
            )
            IonRequestType.ARCHIVE -> throw IllegalStateException("Archive downloads must not be cached")
        }
        return fileWithMeta.file
    }

    class FileWithMeta(
        val file: File,
        val fileTemp: File?,
        val type: IonRequestType?,
        val originUrl: String,
        val archiveIndex: ArchiveIndex,
        val pageIdentifier: String?,
    )
}
