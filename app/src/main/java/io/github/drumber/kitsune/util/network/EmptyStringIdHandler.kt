package io.github.drumber.kitsune.util.network

import com.github.jasminb.jsonapi.ResourceIdHandler

/**
 * Handles empty strings as null.
 */
class EmptyStringIdHandler: ResourceIdHandler {

    override fun asString(identifier: Any?): String? {
        if (identifier != null && identifier.toString().isNotBlank()) {
            return identifier.toString()
        }
        return null
    }

    override fun fromString(source: String?) = source
}