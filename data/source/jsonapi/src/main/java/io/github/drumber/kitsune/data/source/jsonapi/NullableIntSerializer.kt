package io.github.drumber.kitsune.data.source.jsonapi

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

/**
 * [JsonSerializer] that serializes `-1` to `null`.
 */
class NullableIntSerializer : JsonSerializer<Int?>() {

    override fun serialize(value: Int?, gen: JsonGenerator, serializers: SerializerProvider) {
        if (value != null && value == -1) {
            gen.writeNull()
        } else if (value != null) {
            gen.writeNumber(value)
        }
    }
}