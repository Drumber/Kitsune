package io.github.drumber.kitsune.domain.model.infrastructure.algolia

enum class SearchType(private val algoliaKey: (AlgoliaKeyCollection) -> AlgoliaKey?) {
    Users({ it.users }),
    Posts({ it.posts }),
    Media({ it.media }),
    Groups({ it.groups }),
    Characters({ it.characters });

    fun getAlgoliaKey(algoliaKeyCollection: AlgoliaKeyCollection) = algoliaKey(algoliaKeyCollection)
}
