package io.github.drumber.kitsune.data.source.network.media.model.unit

import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.common.Titles

sealed interface NetworkMediaUnit {
    val id: String?

    val description: String?
    val titles: Titles?
    val canonicalTitle: String?

    val number: Int?
    val length: String?
    val thumbnail: Image?
}
