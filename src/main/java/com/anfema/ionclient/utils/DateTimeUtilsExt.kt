package com.anfema.ionclient.utils

// TODO When DateTimeUtils is converted to Kotlin this can be merged

fun String.asDateTimeOrNull() = DateTimeUtils.parseOrNull(this)
