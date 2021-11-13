package io.github.drumber.kitsune.constants

import io.github.drumber.kitsune.data.model.ResourceSelector
import io.github.drumber.kitsune.data.model.ResourceType
import io.github.drumber.kitsune.data.model.SearchParams
import io.github.drumber.kitsune.data.service.Filter

object Defaults {

    val DEFAULT_FILTER get() = Filter().sort(SortFilter.POPULARITY_DESC.queryParam)
    val DEFAULT_RESOURCE_SELECTOR get() = ResourceSelector(ResourceType.Anime, DEFAULT_FILTER)
    val DEFAULT_SEARCH_PARAMS get() = SearchParams(ResourceType.Anime, emptyList(), SortFilter.POPULARITY_DESC)

    /** The minimum of required fields to display resources in a collection, e.g. in RecyclerView. */
    val MINIMUM_COLLECTION_FIELDS get() = arrayOf("slug", "titles", "canonicalTitle", "posterImage", "coverImage")

}