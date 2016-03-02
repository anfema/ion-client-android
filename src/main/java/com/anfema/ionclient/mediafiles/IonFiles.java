package com.anfema.ionclient.mediafiles;

import java.io.File;

import okhttp3.HttpUrl;
import rx.Observable;

public interface IonFiles
{
	Observable<File> request( HttpUrl url, String checksum );

	Observable<File> request( HttpUrl url, String checksum, boolean ignoreCaching, File targetFile );
}
