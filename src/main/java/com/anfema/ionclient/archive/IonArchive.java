package com.anfema.ionclient.archive;

import com.anfema.ionclient.IonConfig;

import io.reactivex.Completable;


public interface IonArchive
{
	/**
	 * Download the archive file for collection (defined by {@link IonConfig}), which should make app usable in offline mode.
	 */
	Completable downloadArchive();
}
