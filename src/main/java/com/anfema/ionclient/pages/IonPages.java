package com.anfema.ionclient.pages;

import com.anfema.ionclient.pages.models.Collection;
import com.anfema.ionclient.pages.models.Page;
import com.anfema.ionclient.pages.models.PagePreview;

import rx.Observable;
import rx.functions.Func1;

public interface IonPages extends ConfigUpdatable
{
	Observable<Collection> fetchCollection();

	Observable<PagePreview> fetchPagePreview( String pageIdentifier );

	Observable<PagePreview> fetchPagePreviews( Func1<PagePreview, Boolean> pagesFilter );

	Observable<PagePreview> fetchAllPagePreviews();

	Observable<Page> fetchPage( String pageIdentifier );

	Observable<Page> fetchPages( Func1<PagePreview, Boolean> pagesFilter );

	Observable<Page> fetchAllPages();
}
