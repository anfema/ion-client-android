package com.anfema.ionclient.serialization

import com.anfema.ionclient.pages.models.Collection
import com.anfema.ionclient.pages.models.Meta
import com.anfema.ionclient.pages.models.Page
import com.anfema.ionclient.pages.models.contents.ConnectionContent
import com.anfema.ionclient.pages.models.contents.Content
import com.google.gson.Gson
import org.joda.time.DateTime

object GsonHolder {
    val plainInstance: Gson = Gson()

    // parse content subtypes
    val defaultInstance: Gson = plainInstance.newBuilder()
        .registerTypeAdapter(Content::class.java, ContentDeserializerFactory.newInstance())
        .registerTypeAdapter(ConnectionContent::class.java,
            ConnectionContentSerializer()) // parse datetime strings (trying two patterns)
        .registerTypeAdapter(DateTime::class.java, DateTimeSerializer()) // parse meta data of page preview
        .registerTypeAdapter(Meta::class.java, MetaSerializer())
        .registerTypeAdapter(Collection::class.java, CollectionDeserializer())
        .registerTypeAdapter(Page::class.java, PageDeserializer())
        .create()
}
