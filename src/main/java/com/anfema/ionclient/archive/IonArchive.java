package com.anfema.ionclient.archive;

import com.anfema.ionclient.IonConfig;

import java.io.File;

import rx.Observable;

public interface IonArchive
{
	/**
	 * Download the archive file for collection (defined by {@link IonConfig}), which should make app usable in offline mode.
	 */
	Observable<File> downloadArchive();
}
