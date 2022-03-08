package com.anfema.utils

/**
 * Makes a ROUGH ESTIMATE about how much memory is required to store a string.
 *
 *
 * Since Java uses unicode, a character uses 2 bytes. Additional overhead is ignored here.
 *
 * @return no. of bytes
 */
fun String.byteCount(): Long =
		(length * 2).toLong()
