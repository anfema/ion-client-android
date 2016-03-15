package com.anfema.ionclient.mediafiles;

import com.anfema.ionclient.pages.ConfigUpdatable;
import com.anfema.ionclient.pages.models.contents.Downloadable;

import java.io.File;

import okhttp3.HttpUrl;
import rx.Observable;

public interface IonFiles extends ConfigUpdatable
{
	Observable<File> request( Downloadable content );

	Observable<File> request( HttpUrl url, String checksum );

	Observable<File> request( HttpUrl url, String checksum, boolean ignoreCaching, File targetFile );
}
