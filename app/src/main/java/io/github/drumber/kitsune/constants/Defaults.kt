package io.github.drumber.kitsune.constants

import io.github.drumber.kitsune.data.model.ResourceSelector
import io.github.drumber.kitsune.data.model.ResourceType
import io.github.drumber.kitsune.data.service.Filter

object Defaults {

    val DEFAULT_FILTER = Filter().sort(SortFilter.POPULARITY_DESC.queryParam)
    val DEFAULT_RESOURCE_SELECTOR = ResourceSelector(ResourceType.Anime, DEFAULT_FILTER)

}