package io.github.drumber.kitsune.data.mapper.graphql

import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.source.graphql.fragment.FullImageFragment

fun FullImageFragment.toImage() = views.toImage(original.url)

fun List<FullImageFragment.View>.toImage(original: String? = null) = Image(
    tiny = firstOrNull { it.name == "tiny" }?.url,
    small = firstOrNull { it.name == "small" }?.url,
    medium = firstOrNull { it.name == "medium" }?.url,
    large = firstOrNull { it.name == "large" }?.url,
    original = original,
    meta = null
)