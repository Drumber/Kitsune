package io.github.drumber.kitsune.data.service

import io.github.drumber.kitsune.constants.Kitsu

class Filter(queryParams: Map<String, String> = emptyMap()) {

    val options = queryParams.toMutableMap()

    /** Defines how much of a resource to receive. The maximum amount is 20. */
    fun pageLimit(limit: Int = Kitsu.DEFAULT_PAGE_SIZE) = put("page[limit]", limit)

    /** The index of the first resource to receive. */
    fun pageOffset(offset: Int = Kitsu.DEFAULT_PAGE_OFFSET) = put("page[offset]", offset)

    /** Return only the specified fields of a resource. */
    fun fields(type: String, vararg fields: String) = put("fields[$type]", fields.joinToString(","))

    /** Sort by the specified attributes. Prepend a `-` for descending order. */
    fun sort(vararg attributes: String) = put("sort", attributes.joinToString(","))

    /** Filter by certain matching attributes or relationships. */
    fun filter(attribute: String, value: String) = put("filter[$attribute]", value)

    fun include(vararg relationships: String) = put("include", relationships.joinToString(","))

    private inline fun put(key: String, value: Any): Filter {
        options[key] = value.toString()
        return this
    }

}