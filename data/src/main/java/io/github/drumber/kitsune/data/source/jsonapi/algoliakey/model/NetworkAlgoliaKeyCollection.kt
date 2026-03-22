package io.github.drumber.kitsune.data.source.jsonapi.algoliakey.model

data class NetworkAlgoliaKeyCollection(
    val users: NetworkAlgoliaKey?,
    val posts: NetworkAlgoliaKey?,
    val media: NetworkAlgoliaKey?,
    val groups: NetworkAlgoliaKey?,
    val characters: NetworkAlgoliaKey?
)
