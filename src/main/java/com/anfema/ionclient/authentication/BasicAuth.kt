package com.anfema.ionclient.authentication

import android.util.Base64
import com.anfema.ionclient.utils.IonLog
import java.io.UnsupportedEncodingException

/**
 * Helper class for basic access authentication.
 */
object BasicAuth {
    /**
     * Generates the value for the "Authorization" header.
     */
    @JvmStatic
    fun getAuthHeaderValue(username: String, password: String): String {
        val usernamePassword = "$username:$password"
        var data: ByteArray? = null
        try {
            data = usernamePassword.toByteArray(charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            IonLog.ex(Exception("Could not get bytes of $usernamePassword", e))
        }
        val base64 = Base64.encodeToString(data, Base64.NO_WRAP)
        return "Basic $base64"
    }
}
