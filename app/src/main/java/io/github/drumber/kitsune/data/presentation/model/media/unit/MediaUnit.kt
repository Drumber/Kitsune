package io.github.drumber.kitsune.data.presentation.model.media.unit

import io.github.drumber.kitsune.data.common.model.Image
import io.github.drumber.kitsune.data.common.model.Titles

sealed interface MediaUnit {
    val id: String?

    val description: String?
    val titles: Titles?
    val canonicalTitle: String?

    val number: Int?
    val length: String?
    val thumbnail: Image?

    val date: String?
}
