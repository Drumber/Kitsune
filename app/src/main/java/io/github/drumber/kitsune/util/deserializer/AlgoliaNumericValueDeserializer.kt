package io.github.drumber.kitsune.util.deserializer

import com.algolia.search.model.filter.Filter.Numeric.Value
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

/**
 * Custom jackson deserializer for [com.algolia.search.model.filter.Filter.Numeric.Value].
 */
class AlgoliaNumericValueDeserializer @JvmOverloads constructor(vc: Class<*>? = null) :
    StdDeserializer<Value>(vc) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Value {
        val node = p.codec.readTree<JsonNode>(p)
        return when {
            isComparison(node) -> p.codec.treeToValue(node, Value.Comparison::class.java)
            isRange(node) -> p.codec.treeToValue(node, Value.Range::class.java)
            else -> throw JsonParseException(
                p,
                "Unsupported type of com.algolia.search.model.filter.Filter.Numeric.Value 'raw' field."
            )
        }
    }

    private fun isComparison(node: JsonNode): Boolean {
        return node.has("operator") && node.has("number")
    }

    private fun isRange(node: JsonNode): Boolean {
        return node.has("lowerBound") && node.has("upperBound")
    }

}