package com.anfema.ampclient.pages;

import com.anfema.ampclient.pages.models.responses.CollectionResponse;
import com.anfema.ampclient.pages.models.responses.PageResponse;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * methods using reactive X pattern returning {@link Observable}
 */
public interface AmpPagesApi
{
	@GET("collections/{collection_identifier}")
	Observable<Response<CollectionResponse>> getCollection( @Path("collection_identifier") String collectionIdentifier, @Query("locale") String locale, @Header("Authorization") String authorizationToken, @Header("If-Modified-Since") String lastModified );

	@GET("pages/{collection_identifier}/{page_identifier}")
	Observable<PageResponse> getPage( @Path("collection_identifier") String collectionIdentifier, @Path("page_identifier") String pageIdentifier, @Query("locale") String locale, @Header("Authorization") String authorizationToken );
}
