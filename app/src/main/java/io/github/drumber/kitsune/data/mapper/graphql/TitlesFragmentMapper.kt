package io.github.drumber.kitsune.data.mapper.graphql

import io.github.drumber.kitsune.data.common.Titles
import io.github.drumber.kitsune.data.source.graphql.fragment.TitlesFragment

fun TitlesFragment.toTitles(): Titles? = localized as? Map<String, String>