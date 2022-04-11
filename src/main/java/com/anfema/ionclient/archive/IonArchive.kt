package com.anfema.ionclient.archive

import io.reactivex.Completable

internal interface IonArchive {

    /**
     * Download the archive file for collection (defined by [com.anfema.ionclient.CollectionProperties]),
     * which makes app usable in offline mode.
     */
    fun downloadArchive(): Completable
}
