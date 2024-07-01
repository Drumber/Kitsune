package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.presentation.model.algolia.AlgoliaKey
import io.github.drumber.kitsune.data.presentation.model.algolia.AlgoliaKeyCollection
import io.github.drumber.kitsune.data.source.network.algolia.model.NetworkAlgoliaKey
import io.github.drumber.kitsune.data.source.network.algolia.model.NetworkAlgoliaKeyCollection

object AlgoliaMapper {
    fun NetworkAlgoliaKeyCollection.toAlgoliaKeyCollection() = AlgoliaKeyCollection(
        users = users?.toAlgoliaKey(),
        posts = posts?.toAlgoliaKey(),
        media = media?.toAlgoliaKey(),
        groups = groups?.toAlgoliaKey(),
        characters = characters?.toAlgoliaKey()
    )

    fun NetworkAlgoliaKey.toAlgoliaKey() = AlgoliaKey(
        key = key.require(),
        index = index
    )
}