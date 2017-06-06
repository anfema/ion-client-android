package com.anfema.ionclient.pages;

import com.anfema.ionclient.pages.models.Collection;
import com.anfema.ionclient.pages.models.Page;
import com.anfema.ionclient.pages.models.PagePreview;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Predicate;

public interface IonPages extends ConfigUpdatable
{
	Single<Collection> fetchCollection();

	Single<PagePreview> fetchPagePreview( String pageIdentifier );

	Observable<PagePreview> fetchPagePreviews( Predicate<PagePreview> pagesFilter );

	Observable<PagePreview> fetchAllPagePreviews();

	Single<Page> fetchPage( String pageIdentifier );

	Observable<Page> fetchPages( List<String> pageIdentifiers );

	Observable<Page> fetchPages( Predicate<PagePreview> pagesFilter );

	Observable<Page> fetchAllPages();
}
