package com.anfema.ampclient.pages;

import com.anfema.ampclient.pages.models.responses.CollectionResponse;
import com.anfema.ampclient.pages.models.responses.PageResponse;

import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * methods using reactive X pattern returning {@link Observable}
 */
public interface AmpPagesApi
{
	@GET("collections/{collection_identifier}")
	Observable<CollectionResponse> getCollection( @Path("collection_identifier") String collectionIdentifier, @Query("locale") String locale, @Header("Authorization") String authorizationToken );

	@GET("pages/{collection_identifier}/{page_identifier}")
	Observable<PageResponse> getPage( @Path("collection_identifier") String collectionIdentifier, @Path("page_identifier") String pageIdentifier, @Query("locale") String locale, @Header("Authorization") String authorizationToken );
}
