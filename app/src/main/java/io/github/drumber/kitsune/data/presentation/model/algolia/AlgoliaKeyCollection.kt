package io.github.drumber.kitsune.data.presentation.model.algolia

data class AlgoliaKeyCollection(
    val users: AlgoliaKey?,
    val posts: AlgoliaKey?,
    val media: AlgoliaKey?,
    val groups: AlgoliaKey?,
    val characters: AlgoliaKey?
)
