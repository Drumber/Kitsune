package io.github.drumber.kitsune.data.source.jsonapi.media.model.unit

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.model.Image
import io.github.drumber.kitsune.data.model.Titles

@Type("episodes")
data class NetworkEpisode(
    @Id
    override val id: String?,

    override val description: String?,
    override val titles: Titles?,
    override val canonicalTitle: String?,

    override val number: Int?,
    val seasonNumber: Int?,
    val relativeNumber: Int?,
    override val length: String?,
    val airdate: String?,

    override val thumbnail: Image?
) : NetworkMediaUnit
