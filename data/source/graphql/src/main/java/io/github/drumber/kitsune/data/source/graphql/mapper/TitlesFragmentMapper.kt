package io.github.drumber.kitsune.data.source.graphql.mapper

import io.github.drumber.kitsune.data.common.model.Titles
import io.github.drumber.kitsune.data.source.graphql.fragment.TitlesFragment

fun TitlesFragment.toTitles(): Titles? {
    var localized = (localized as? Map<String, String>)?.mapKeys { it.key.replace('-', '_') }
    if (!romanized.isNullOrBlank()) {
        // add romanized title to titles map for backwards compatibility
        localized = (localized ?: emptyMap()) + mapOf("en_jp" to romanized)
    }
    return localized
}