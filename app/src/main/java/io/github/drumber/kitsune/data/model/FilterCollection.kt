package io.github.drumber.kitsune.data.model

import com.algolia.instantsearch.filter.state.FilterGroupID
import com.algolia.instantsearch.filter.state.Filters
import com.algolia.search.model.filter.Filter

data class FilterCollection(
    val facetGroups: List<FilterCollectionEntry<Filter.Facet>> = emptyList(),
    val tagGroups: List<FilterCollectionEntry<Filter.Tag>> = emptyList(),
    val numericGroups: List<FilterCollectionEntry<Filter.Numeric>> = emptyList()
)

data class FilterCollectionEntry<T : Filter>(
    val filterGroupID: FilterGroupID,
    val filters: Set<T>
)

fun Filters.toFilterCollection() = FilterCollection(
    getFacetGroups().toEntryList(),
    getTagGroups().toEntryList(),
    getNumericGroups().toEntryList()
)

fun FilterCollection.toCombinedMap(): Map<FilterGroupID, Set<Filter>> {
    return facetGroups.toMap() + tagGroups.toMap() + numericGroups.toMap()
}

private fun <T : Filter> Map<FilterGroupID, Set<T>>.toEntryList(): List<FilterCollectionEntry<T>> {
    return this.map {
        FilterCollectionEntry(it.key, it.value)
    }
}

private fun <T : Filter> List<FilterCollectionEntry<T>>.toMap(): Map<FilterGroupID, Set<T>> {
    return buildMap {
        this@toMap.forEach { entry ->
            put(entry.filterGroupID, entry.filters)
        }
    }
}
