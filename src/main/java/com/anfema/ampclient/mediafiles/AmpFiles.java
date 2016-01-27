package com.anfema.ampclient.mediafiles;

import java.io.File;

import okhttp3.HttpUrl;
import rx.Observable;

public interface AmpFiles
{
	Observable<File> request( HttpUrl url );

	Observable<File> request( HttpUrl url, File targetFile );
}
