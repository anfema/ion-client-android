package com.anfema.ionclient.archive

import android.content.Context
import com.anfema.ionclient.IonConfig
import com.anfema.ionclient.caching.FilePaths
import com.anfema.ionclient.caching.index.FileCacheIndex
import com.anfema.ionclient.exceptions.HttpException
import com.anfema.ionclient.mediafiles.FileWithStatus
import com.anfema.ionclient.mediafiles.IonFiles
import com.anfema.ionclient.pages.CollectionDownloadedListener
import com.anfema.ionclient.pages.IonPages
import com.anfema.ionclient.pages.IonPagesWithCaching
import com.anfema.ionclient.pages.models.Collection
import com.anfema.ionclient.utils.DateTimeUtils
import com.anfema.ionclient.utils.IonLog
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File
import java.net.HttpURLConnection

internal class IonArchiveDownloader(
    private val ionPages: IonPages,
    private val ionFiles: IonFiles,
    private val config: IonConfig,
    private val context: Context,
) : IonArchive, CollectionDownloadedListener {
    /**
     * Prevent multiple archive downloads at the same time.
     */
    var activeArchiveDownload = false

    /**
     * Download the archive file for current collection, which should make app usable in offline mode.
     */
    override fun downloadArchive(): Completable =
        downloadArchive(null, null)

    /**
     * Download the archive file for current collection, which should make app usable in offline mode.
     *
     * @param inCollection If collection already is available it can be passed in order to save time.
     * @param lastModified when the collection has been last modified
     */
    private fun downloadArchive(inCollection: Collection?, lastModified: String?): Completable {

        if (inCollection != null && inCollection.identifier != config.collectionIdentifier) {
            val e =
                Exception("Archive download: inCollection.identifier: " + inCollection.identifier + " does not match config's collectionIdentifier: " + config.collectionIdentifier)
            IonLog.ex(e)
            return Completable.error(e)
        }

        val archivePath = FilePaths.getArchiveFilePath(config, context)
        IonLog.i("ION Archive", "about to download archive for collection " + config.collectionIdentifier)
        activeArchiveDownload = true

        // use inCollection or retrieve by making a collections call
        val collectionObs: Single<Collection> =
            if (inCollection == null) {
                ionPages.fetchCollection(true)
            } else {
                Single.just(inCollection)
            }

        val archiveRequestTime = DateTimeUtils.now()
        return collectionObs.flatMap { collection: Collection ->
            // download archive
            ionFiles.request(
                url = collection.archive.toHttpUrl(),
                downloadUrl = getDownloadUrl(collection.archive),
                checksum = null,
                ignoreCaching = true,
                targetFile = archivePath,
            )
                .map { fileWithStatus: FileWithStatus -> fileWithStatus.file }
                .map { archiveFile: File -> CollectionArchive(collection, archiveFile) }
        }
            .flatMapObservable { collArch: CollectionArchive ->
                // untar archive
                ArchiveUtils.unTar(
                    collArch.archivePath,
                    collArch.collection,
                    lastModified,
                    archiveRequestTime,
                    config,
                    context
                )
            }
            .ignoreElements()
            .onErrorComplete { ex: Throwable? -> ex is HttpException && ex.code == HttpURLConnection.HTTP_NOT_MODIFIED }
            .doFinally { activeArchiveDownload = false }
            .subscribeOn(Schedulers.io())
    }

    private fun getDownloadUrl(archive: String): HttpUrl =
        (archive + addLastUpdated(archive)).toHttpUrl()

    private fun addLastUpdated(archive: String): String {
        val fileCacheIndex = FileCacheIndex.retrieve(archive, config, context)

        return if (fileCacheIndex != null) {
            val divider = if (archive.toHttpUrl().querySize > 0) "&" else "?"
            divider + "lastUpdated=" + DateTimeUtils.toString(fileCacheIndex.lastUpdated)
        } else {
            ""
        }
    }

    /**
     * Check if archive needs to be updated.
     */
    override fun collectionDownloaded(collection: Collection, lastModified: String) {
        if (config.archiveDownloads && !activeArchiveDownload) {
            // archive needs to be downloaded again. Download runs in background and does not even inform UI when finished
            downloadArchive(collection, lastModified)
                .subscribe(
                    { IonLog.d("ION Archive", "Archive has been downloaded/updated in background") },
                    IonLog::ex,
                )
        }
    }

    private class CollectionArchive(val collection: Collection, val archivePath: File)

    init {

        // if ionPages uses caching provide archive update check when collection downloaded
        if (ionPages is IonPagesWithCaching) {
            ionPages.setCollectionListener(this)
        }
    }
}
