package io.github.drumber.kitsune.constants

import io.github.drumber.kitsune.domain.model.MediaSelector
import io.github.drumber.kitsune.domain.model.MediaType
import io.github.drumber.kitsune.domain.model.SearchParams
import io.github.drumber.kitsune.domain.service.Filter

object Defaults {

    val DEFAULT_FILTER get() = Filter().sort(SortFilter.POPULARITY_DESC.queryParam)
    val DEFAULT_MEDIA_SELECTOR get() = MediaSelector(MediaType.Anime, DEFAULT_FILTER)
    val DEFAULT_SEARCH_PARAMS get() = SearchParams(MediaType.Anime, emptyList(), SortFilter.POPULARITY_DESC)

    /** The minimum of required fields to display resources in a collection, e.g. in RecyclerView. */
    val MINIMUM_COLLECTION_FIELDS get() = arrayOf("slug", "titles", "canonicalTitle", "posterImage", "coverImage")

}