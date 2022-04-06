package com.anfema.ionclient.utils

import android.util.Log

object IonLog {

    const val VERBOSE = Log.VERBOSE
    const val DEBUG = Log.DEBUG
    const val INFO = Log.INFO
    const val WARN = Log.WARN
    const val ERROR = Log.ERROR
    const val NONE = Int.MAX_VALUE

    /** Set log level for ION client */
    var logLevel = NONE

    /** set the default Tag used when functions are called, where no log tag is provided */
    var defaultTag = "IonClient"

    @JvmStatic
    fun v(tag: String, message: String) {
        if (logLevel == VERBOSE) {
            Log.v(tag, message)
        }
    }

    @JvmStatic
    fun v(message: String) {
        v(defaultTag, message)
    }

    @JvmStatic
    fun d(tag: String, message: String) {
        if (logLevel in VERBOSE..DEBUG) {
            Log.d(tag, message)
        }
    }

    @JvmStatic
    fun d(message: String) {
        d(defaultTag, message)
    }

    @JvmStatic
    fun i(tag: String, message: String) {
        if (logLevel in VERBOSE..INFO) {
            Log.i(tag, message)
        }
    }

    @JvmStatic
    fun i(message: String) {
        i(defaultTag, message)
    }

    @JvmStatic
    fun w(tag: String, message: String) {
        if (logLevel in VERBOSE..WARN) {
            Log.w(tag, message)
        }
    }

    @JvmStatic
    fun w(message: String) {
        w(defaultTag, message)
    }

    @JvmStatic
    fun e(tag: String, message: String) {
        if (logLevel in VERBOSE..ERROR) {
            Log.e(tag, message)
        }
    }

    @JvmStatic
    fun e(message: String) {
        e(defaultTag, message)
    }

    @JvmStatic
    fun ex(tag: String, exception: Throwable) {
        if (logLevel in VERBOSE..ERROR) {
            Log.e(tag, Log.getStackTraceString(exception))
        }
    }

    @JvmStatic
    fun ex(exception: Throwable) {
        ex(defaultTag, exception)
    }
}
