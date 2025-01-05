package io.github.drumber.kitsune.data.presentation.model.media.unit

import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.common.model.Image
import io.github.drumber.kitsune.data.common.model.Titles

data class Chapter(
    override val id: String,

    override val description: String?,
    override val titles: Titles?,
    override val canonicalTitle: String?,

    override val number: Int?,
    val volumeNumber: Int?,
    override val length: String?,

    override val thumbnail: Image?,
    val published: String?
) : MediaUnit {

    override val numberStringRes
        get() = R.string.unit_chapter

    override val lengthStringRes: Int
        get() = R.plurals.unit_pages

    override val date: String?
        get() = published
}
