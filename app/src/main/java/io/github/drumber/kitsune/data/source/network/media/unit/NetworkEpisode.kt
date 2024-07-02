package io.github.drumber.kitsune.data.source.network.media.unit

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.common.Titles

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
