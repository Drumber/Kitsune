package io.github.drumber.kitsune.util

import io.github.drumber.kitsune.constants.Kitsu
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat
import java.util.*

object SearchResourceProcessor {

    fun processSearchResource(json: JsonObject): JsonObject {
        val jsonMap = json.toMutableMap()
        fixDescription(jsonMap)
        fixDates(jsonMap)
        return JsonObject(jsonMap)
    }

    // replace description object with description string
    private fun fixDescription(json: MutableMap<String, JsonElement>) {
        val descriptionText: String? = (json["description"] as? JsonObject)?.let { descriptions ->
            if(descriptions.containsKey("en")) {
                descriptions["en"]?.jsonPrimitive?.content
            } else {
                descriptions.values.firstOrNull()?.jsonPrimitive?.content
            }
        }
        json["description"] = JsonPrimitive(descriptionText)
    }

    // convert seconds to time stamp
    private fun fixDates(json: MutableMap<String, JsonElement>) {
        json["startDate"] = json["startDate"].toDateString()
        json["endDate"] = json["endDate"].toDateString()
    }

    private inline fun JsonElement?.toDateString(): JsonPrimitive {
        return this?.jsonPrimitive?.longOrNull?.let { JsonPrimitive(it.toDateString()) } ?: JsonNull
    }

    private inline fun Long.toDateString(): String {
        val date = Date(this * 1000L)
        return SimpleDateFormat(Kitsu.DEFAULT_DATE_FORMAT).format(date)
    }

}