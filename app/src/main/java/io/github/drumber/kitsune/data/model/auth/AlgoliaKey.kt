package io.github.drumber.kitsune.data.model.auth

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AlgoliaKey(
    val key: String? = null,
    val index: String? = null
): Parcelable

@Parcelize
data class AlgoliaKeyCollection(
    val users: AlgoliaKey? = null,
    val posts: AlgoliaKey? = null,
    val media: AlgoliaKey? = null,
    val groups: AlgoliaKey? = null,
    val characters: AlgoliaKey? = null
): Parcelable
