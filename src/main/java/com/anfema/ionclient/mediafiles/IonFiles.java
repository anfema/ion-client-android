package com.anfema.ionclient.mediafiles;

import com.anfema.ionclient.pages.ConfigUpdatable;
import com.anfema.ionclient.pages.models.contents.Downloadable;

import java.io.File;

import io.reactivex.Single;
import okhttp3.HttpUrl;

public interface IonFiles extends ConfigUpdatable
{
	Single<File> request( Downloadable content );

	Single<File> request( HttpUrl url, String checksum );

	Single<File> request( HttpUrl url, String checksum, boolean ignoreCaching, File targetFile );
}
