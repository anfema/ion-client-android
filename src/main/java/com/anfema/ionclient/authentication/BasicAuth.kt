package com.anfema.ionclient.authentication

import okhttp3.Credentials

/**
 * Helper class for basic access authentication.
 */
object BasicAuth {
    /**
     * Generates the value for the "Authorization" header.
     */
    @JvmStatic
    @Deprecated("Use implementation from OkHttp",
        ReplaceWith("Credentials.basic(username, password)", "okhttp3.Credentials"))
    fun getAuthHeaderValue(username: String, password: String): String =
        Credentials.basic(username, password)
}
