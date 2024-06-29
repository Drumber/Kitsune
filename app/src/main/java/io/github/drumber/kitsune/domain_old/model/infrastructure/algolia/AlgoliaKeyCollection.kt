package io.github.drumber.kitsune.domain_old.model.infrastructure.algolia

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AlgoliaKeyCollection(
    val users: AlgoliaKey? = null,
    val posts: AlgoliaKey? = null,
    val media: AlgoliaKey? = null,
    val groups: AlgoliaKey? = null,
    val characters: AlgoliaKey? = null
) : Parcelable
