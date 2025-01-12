package io.github.drumber.kitsune.data.model.media.unit

import io.github.drumber.kitsune.data.model.Image
import io.github.drumber.kitsune.data.model.Titles

data class Episode(
    override val id: String,

    override val description: String?,
    override val titles: Titles?,
    override val canonicalTitle: String?,

    override val number: Int?,
    val seasonNumber: Int?,
    val relativeNumber: Int?,
    override val length: String?,
    val airdate: String?,

    override val thumbnail: Image?
) : MediaUnit {

    override val date: String?
        get() = airdate
}
