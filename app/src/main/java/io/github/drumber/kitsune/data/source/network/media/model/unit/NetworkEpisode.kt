package io.github.drumber.kitsune.data.source.network.media.model.unit

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.common.Titles
import io.github.drumber.kitsune.data.source.network.NetworkImage

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

    override val thumbnail: NetworkImage?
) : NetworkMediaUnit {

    companion object {
        fun empty(id: String) = NetworkEpisode(
            id = id,
            description = null,
            titles = null,
            canonicalTitle = null,
            number = null,
            seasonNumber = null,
            relativeNumber = null,
            length = null,
            airdate = null,
            thumbnail = null
        )
    }
}
