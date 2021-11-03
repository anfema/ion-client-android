package com.anfema.ionclient.pages.models.contents

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Connection(
    private val connectionString: String,
) : Parcelable {

    val url: Uri
        get() = Uri.parse(connectionString)

    val pageIdentifier: String?
        get() = url.run { pathSegments.lastOrNull() ?: host }
}
