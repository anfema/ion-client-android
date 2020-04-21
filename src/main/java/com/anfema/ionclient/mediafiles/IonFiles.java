package com.anfema.ionclient.mediafiles;

import androidx.annotation.Nullable;

import com.anfema.ionclient.pages.ConfigUpdatable;
import com.anfema.ionclient.pages.models.contents.Downloadable;

import java.io.File;

import io.reactivex.Single;
import okhttp3.HttpUrl;

public interface IonFiles extends ConfigUpdatable
{
	Single<FileWithStatus> request( Downloadable content );

	Single<FileWithStatus> request( HttpUrl url, String checksum );

	Single<FileWithStatus> request( HttpUrl url, String checksum, boolean ignoreCaching, @Nullable File targetFile );

	Single<FileWithStatus> request( HttpUrl url, HttpUrl downloadUrl, String checksum, boolean ignoreCaching, @Nullable File inTargetFile );
}
