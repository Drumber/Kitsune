package io.github.drumber.kitsune.data.source.jsonapi.media.model.unit

import io.github.drumber.kitsune.data.model.Image
import io.github.drumber.kitsune.data.model.Titles

sealed interface NetworkMediaUnit {
    val id: String?

    val description: String?
    val titles: Titles?
    val canonicalTitle: String?

    val number: Int?
    val length: String?
    val thumbnail: Image?
}
