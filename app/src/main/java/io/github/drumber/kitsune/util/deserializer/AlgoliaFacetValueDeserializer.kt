package io.github.drumber.kitsune.util.deserializer

import com.algolia.search.model.filter.Filter.Facet.Value
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.JsonNodeType

/**
 * Custom jackson deserializer for [com.algolia.search.model.filter.Filter.Facet.Value].
 */
class AlgoliaFacetValueDeserializer @JvmOverloads constructor(vc: Class<*>? = null) :
    StdDeserializer<Value>(vc) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Value {
        val node = p.codec.readTree<JsonNode>(p)
        return when (node.get("raw").nodeType) {
            JsonNodeType.STRING -> p.codec.treeToValue(node, Value.String::class.java)
            JsonNodeType.BOOLEAN -> p.codec.treeToValue(node, Value.Boolean::class.java)
            JsonNodeType.NUMBER -> p.codec.treeToValue(node, Value.Number::class.java)
            else -> throw JsonParseException(
                p,
                "Unsupported type of com.algolia.search.model.filter.Filter.Facet.Value 'raw' field."
            )
        }
    }
}