package com.anfema.ampclient.archive;

import com.anfema.ampclient.AmpConfig;

import java.io.File;

import rx.Observable;

public interface AmpArchive
{
	/**
	 * Download the archive file for collection (defined by {@link AmpConfig}), which should make app usable in offline mode.
	 */
	Observable<File> downloadArchive();
}
