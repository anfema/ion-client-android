package com.anfema.ionclient.archive;

import com.anfema.ionclient.IonConfig;
import com.anfema.ionclient.pages.ConfigUpdatable;

import java.io.File;

import io.reactivex.Observable;


public interface IonArchive extends ConfigUpdatable
{
	/**
	 * Download the archive file for collection (defined by {@link IonConfig}), which should make app usable in offline mode.
	 */
	Observable<File> downloadArchive();
}
