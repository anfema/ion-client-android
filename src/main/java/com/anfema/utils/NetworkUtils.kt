package com.anfema.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.core.content.getSystemService

object NetworkUtils {

    @JvmStatic
    fun isConnected(context: Context): Boolean =
        getNetworkInfo(context)?.isConnected == true

    private fun getNetworkInfo(context: Context): NetworkInfo? =
        context.getSystemService<ConnectivityManager>()?.activeNetworkInfo
}
