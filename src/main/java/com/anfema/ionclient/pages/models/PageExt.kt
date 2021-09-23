package com.anfema.ionclient.pages.models

import com.anfema.ionclient.pages.models.contents.Content

/**
 * Convenience function to access specific Content subtype T
 */
inline fun <reified T : Content> Page.get(outlet: String): T? = getContent(outlet, T::class.java)
