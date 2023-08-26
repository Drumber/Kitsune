package io.github.drumber.kitsune.domain.service

import android.os.Parcelable
import io.github.drumber.kitsune.constants.Kitsu
import kotlinx.parcelize.Parcelize

@Parcelize
data class Filter(val options: MutableMap<String, String> = mutableMapOf()): Parcelable {

    /** Defines how much of data to receive. This is only used for some lists, like trending. */
    fun limit(limit: Int) = put("limit", limit)

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

    /** Checks if there is a filter applied for the given attribute name. */
    fun hasFilterAttribute(attribute: String) = options.containsKey("filter[$attribute]")

    fun include(vararg relationships: String) = put("include", relationships.joinToString(","))

    private fun put(key: String, value: Any): Filter {
        options[key] = value.toString()
        return this
    }

}