package com.anfema.ionclient.pages;

import com.anfema.ionclient.pages.models.responses.CollectionResponse;
import com.anfema.ionclient.pages.models.responses.PageResponse;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * methods using reactive X pattern returning {@link Observable}
 */
public interface IonPagesApi
{
	@GET("{locale}/{collection_identifier}")
	Observable<Response<CollectionResponse>> getCollection( @Path("collection_identifier") String collectionIdentifier, @Path("locale") String locale, @Header("Authorization") String authorizationToken, @Query("variation") String variation, @Header("If-Modified-Since") String lastModified );

	@GET("{locale}/{collection_identifier}/{page_identifier}")
	Observable<PageResponse> getPage( @Path("collection_identifier") String collectionIdentifier, @Path("page_identifier") String pageIdentifier, @Path("locale") String locale, @Query("variation") String variation, @Header("Authorization") String authorizationToken );
}
