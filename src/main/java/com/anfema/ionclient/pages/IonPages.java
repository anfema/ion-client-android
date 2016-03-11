package com.anfema.ionclient.pages;

import com.anfema.ionclient.pages.models.Collection;
import com.anfema.ionclient.pages.models.Page;
import com.anfema.ionclient.pages.models.PagePreview;

import rx.Observable;
import rx.functions.Func1;

public interface IonPages extends ConfigUpdatable
{
	Observable<Collection> getCollection();

	Observable<Page> getPage( String pageIdentifier );

	Observable<Page> getAllPages();

	Observable<Page> getPages( Func1<PagePreview, Boolean> pagesFilter );

	Observable<Page> getPagesSorted( Func1<PagePreview, Boolean> pagesFilter );

	Observable<PagePreview> getPagePreview( String pageIdentifier );

	Observable<PagePreview> getPagePreviews( Func1<PagePreview, Boolean> pagesFilter );

	Observable<PagePreview> getPagePreviewsSorted( Func1<PagePreview, Boolean> pagesFilter );
}
