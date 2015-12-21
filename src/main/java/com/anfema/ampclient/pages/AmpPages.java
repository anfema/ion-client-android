package com.anfema.ampclient.pages;

import com.anfema.ampclient.models.Collection;
import com.anfema.ampclient.models.Page;
import com.anfema.ampclient.models.PagePreview;

import rx.Observable;
import rx.functions.Func1;

public interface AmpPages
{
	Observable<Collection> getCollection();

	Observable<Page> getPage( String pageIdentifier );

	Observable<Page> getAllPages();

	Observable<Page> getPages( Func1<PagePreview, Boolean> pagesFilter );

	Observable<Page> getPagesOrdered( Func1<PagePreview, Boolean> pagesFilter );
}
