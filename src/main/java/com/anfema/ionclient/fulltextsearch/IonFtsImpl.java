package com.anfema.ionclient.fulltextsearch;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.widget.Toast;

import com.anfema.ionclient.IonClient;
import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.R;
import com.anfema.ionclient.mediafiles.IonFiles;
import com.anfema.ionclient.pages.IonPages;
import com.anfema.ionclient.utils.Log;
import com.anfema.ionclient.utils.RxUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import rx.Observable;

/**
 * Full text search on collectin data.
 * <p/>
 * Accessible via {@link IonClient}
 */
class IonFtsImpl implements IonFts
{
	private final IonPages  ionPages;
	private final IonFiles  ionFiles;
	private final IonConfig config;
	private final Context   context;

	public IonFtsImpl( IonPages ionPages, IonFiles ionFiles, IonConfig config, Context context )
	{
		this.ionPages = ionPages;
		this.ionFiles = ionFiles;
		this.config = config;
		this.context = context;
	}

	/**
	 * @see IonFts#downloadSearchDatabase() (String, String, String)
	 */
	@Override
	public Observable<File> downloadSearchDatabase()
	{
		File dbTargetPath = FtsDbUtils.getPath( config.collectionIdentifier, context );
		Log.i( "FTS Database", "about to download FTS database for collection " + config.collectionIdentifier );
		return ionPages.getCollection()
				.map( collection -> collection.fts_db )
				.flatMap( searchDbUrl -> ionFiles.request( HttpUrl.parse( searchDbUrl ), null, true, dbTargetPath ) );
	}

	/**
	 * @see IonFts#fullTextSearch(String, String, String)
	 */
	@Override
	public Observable<List<SearchResult>> fullTextSearch( String searchTerm, String locale, String pageLayout )
	{
		return Observable.just( null )
				.map( o -> performFts( searchTerm, locale, pageLayout ) )
				.compose( RxUtils.runOnComputionThread() );
	}

	private List<SearchResult> performFts( String searchTerm, String locale, String pageLayout )
	{
		List<SearchResult> results = new ArrayList<>();
		Cursor cursor = queryFts( searchTerm, locale, pageLayout );
		if ( cursor != null )
		{
			if ( cursor.moveToFirst() )
			{
				do
				{
					results.add( new SearchResult( cursor.getString( 0 ), cursor.getString( 1 ), cursor.getString( 2 ) ) );
				}
				while ( cursor.moveToNext() );
			}
			cursor.close();
		}
		return results;
	}

	private Cursor queryFts( String searchTerm, String locale, String pageLayout )
	{
		searchTerm = prepareSearchTerm( searchTerm );

		try
		{
			SQLiteDatabase ftsDb = new FtsDbHelper( config.collectionIdentifier, context ).getReadableDatabase();

			List<String> args = new ArrayList<>();
			args.add( locale );

			String additionalFilters = "";
			boolean searchTermFilter = !searchTerm.equals( "" );
			if ( searchTermFilter )
			{
				additionalFilters += context.getString( R.string.fts_search_term_filter );
				args.add( searchTerm );
			}

			boolean layoutFilter = pageLayout != null;
			if ( layoutFilter )
			{
				additionalFilters += context.getString( R.string.fts_page_layout_filter );
				args.add( pageLayout );
			}

			String ftsQuery = context.getString( R.string.fts_query, additionalFilters );
			String[] argsArray = args.toArray( new String[ args.size() ] );

			return ftsDb.rawQuery( ftsQuery, argsArray );
		}
		catch ( SQLiteException e )
		{
			Log.ex( "Full Text Search", e );
			Toast.makeText( context, context.getString( R.string.fts_not_available ), Toast.LENGTH_SHORT ).show();
			return null;
		}
	}

	/**
	 * Appends '*' character at the end of every word
	 *
	 * @param searchTerm
	 * @return
	 */
	String prepareSearchTerm( String searchTerm )
	{
		if ( searchTerm == null || searchTerm.equals( "" ) )
		{
			return "";
		}

		StringBuilder searchTermModified = new StringBuilder();
		String[] words = searchTerm.split( " " );
		for ( String word : words )
		{
			searchTermModified.append( word ).append( "* " );
		}
		return searchTermModified.toString();
	}

	private void testSearch()
	{
		downloadSearchDatabase()
				.subscribe( o -> {
					String personLayout = "person-layout";
					String eventLayout = "event-layout";
					fullTextSearch( "Hab M", "de_DE", personLayout )
							.subscribe( results -> {
								Log.d( "****FTS***", "#results: " + results.size() );
								for ( SearchResult result : results )
								{
									Log.d( "*******FTS********", result.pageIdentifier + ", " + result.text + ", " + result.pageLayout );
								}
							} );
				}, RxUtils.DEFAULT_EXCEPTION_HANDLER );

	}
}