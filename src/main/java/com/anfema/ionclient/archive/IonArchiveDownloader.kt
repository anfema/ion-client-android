package com.anfema.ionclient.archive

import android.content.Context
import com.anfema.ionclient.IonConfig
import com.anfema.ionclient.caching.FilePaths
import com.anfema.ionclient.caching.index.FileCacheIndex
import com.anfema.ionclient.exceptions.HttpException
import com.anfema.ionclient.mediafiles.FileWithStatus
import com.anfema.ionclient.mediafiles.IonFiles
import com.anfema.ionclient.pages.IonPages
import com.anfema.ionclient.pages.models.Collection
import com.anfema.ionclient.utils.DateTimeUtils
import com.anfema.ionclient.utils.IonLog
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposables
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
) : IonArchive {
    /**
     * Prevent multiple archive downloads at the same time.
     */
    var activeArchiveDownload = false
    private var backgroundDownloadDisposable = Disposables.disposed()

    init {
        if (config.automaticArchiveDownloads) {
            ionPages.onCollectionDownloaded
                .doOnNext { downloadArchiveInBackground(it.collection, it.lastModified) }
                .subscribe()
        }
    }

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
            val archiveUrl = collection.archive.toHttpUrl()
            ionFiles.request(
                url = archiveUrl,
                downloadUrl = getDownloadUrl(collection, archiveUrl),
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

    private class CollectionArchive(val collection: Collection, val archivePath: File)

    private fun getDownloadUrl(collection: Collection, archiveUrl: HttpUrl) =
        getLastUpdatedValue(collection.archive)?.let {
            archiveUrl.newBuilder().addQueryParameter("lastUpdated", it).build()
        } ?: archiveUrl

    private fun getLastUpdatedValue(archive: String): String? {
        val fileCacheIndex = FileCacheIndex.retrieve(archive, config, context)
        return fileCacheIndex?.let { DateTimeUtils.toString(it.lastUpdated) }
    }

    /**
     * Download archive in background and do not inform UI when finished
     */
    private fun downloadArchiveInBackground(collection: Collection, lastModified: String?) {
        if (!activeArchiveDownload) {
            // There could still be a parallel foreground archive download
            backgroundDownloadDisposable.dispose()
            backgroundDownloadDisposable = downloadArchive(collection, lastModified)
                .subscribe(
                    { IonLog.d("ION Archive", "Archive has been downloaded/updated in background") },
                    IonLog::ex,
                )
        }
    }
}
