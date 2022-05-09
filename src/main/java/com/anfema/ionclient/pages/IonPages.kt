package com.anfema.ionclient.pages

import com.anfema.ionclient.pages.models.Collection
import com.anfema.ionclient.pages.models.Page
import com.anfema.ionclient.pages.models.PagePreview
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Predicate

internal interface IonPages {

    val onCollectionDownloaded: Observable<CollectionDownloaded>

    fun fetchCollection(): Single<Collection>
    fun fetchCollection(preferNetwork: Boolean): Single<Collection>

    fun fetchPagePreview(pageIdentifier: String): Single<PagePreview>
    fun fetchPagePreviews(pagesFilter: Predicate<PagePreview>): Observable<PagePreview>
    fun fetchAllPagePreviews(): Observable<PagePreview>

    fun fetchPage(pageIdentifier: String): Single<Page>

    fun fetchPages(pageIdentifiers: List<String>): Observable<Page>
    fun fetchPages(pagesFilter: Predicate<PagePreview>): Observable<Page>
    fun fetchAllPages(): Observable<Page>
}

internal class CollectionDownloaded(val collection: Collection, val lastModified: String?)

