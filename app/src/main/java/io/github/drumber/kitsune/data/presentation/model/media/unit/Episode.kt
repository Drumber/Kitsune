package io.github.drumber.kitsune.data.presentation.model.media.unit

import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.common.Titles

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

    override val numberStringRes
        get() = R.string.unit_episode

    override val lengthStringRes: Int
        get() = R.plurals.duration_minutes

    override val date: String?
        get() = airdate
}
