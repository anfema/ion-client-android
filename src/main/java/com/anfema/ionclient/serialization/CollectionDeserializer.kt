package com.anfema.ionclient.serialization

import com.anfema.ionclient.pages.models.Collection
import com.anfema.ionclient.pages.models.Meta
import com.anfema.ionclient.serialization.GsonHolder.plainInstance
import com.anfema.utils.ListUtils
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import org.joda.time.DateTime
import java.lang.reflect.Type

/**
 * Hook into deserialization of [Collection] to sort page previews by position.
 */
class CollectionDeserializer : JsonDeserializer<Collection> {

    companion object {
        private val gson = plainInstance.newBuilder()
            // parse datetime strings
            .registerTypeAdapter(DateTime::class.java, DateTimeSerializer())
            // parse meta data of page preview
            .registerTypeAdapter(Meta::class.java, MetaSerializer())
            .create()
    }

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Collection {
        val collection = gson.fromJson(json, Collection::class.java)
        ListUtils.sort(collection.pages)
        return collection
    }
}
