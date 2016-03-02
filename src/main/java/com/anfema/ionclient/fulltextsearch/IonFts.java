package com.anfema.ionclient.fulltextsearch;

import java.io.File;
import java.util.List;

import rx.Observable;

public interface IonFts
{
	/**
	 * download full text search database for respective collection
	 */
	Observable<File> downloadSearchDatabase();

	/**
	 * Precondition: FTS database has been downloaded via {@link #downloadSearchDatabase()} first.
	 * <p>
	 * Perform a full text search.
	 *
	 * @param searchTerm optional filter. can contain multiple terms separated by {@code " "}.
	 *                   No filter on text is applied if {@code null} or empty String.
	 *                   Search terms must be prefixes to words.
	 * @param locale     obligatory, e.g. "de_DE"
	 * @param pageLayout optional filter by page layout. no filter applied if {@code null}
	 * @return all found entries
	 */
	Observable<List<SearchResult>> fullTextSearch( String searchTerm, String locale, String pageLayout );
}
