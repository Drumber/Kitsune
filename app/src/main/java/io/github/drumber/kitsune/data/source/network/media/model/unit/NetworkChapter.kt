package io.github.drumber.kitsune.data.source.network.media.model.unit

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.common.Titles
import io.github.drumber.kitsune.data.source.network.NetworkImage

@Type("chapters")
data class NetworkChapter(
    @Id
    override val id: String?,

    override val description: String?,
    override val titles: Titles?,
    override val canonicalTitle: String?,

    override val number: Int?,
    val volumeNumber: Int?,
    override val length: String?,

    override val thumbnail: NetworkImage?,
    val published: String?
) : NetworkMediaUnit {

    companion object {
        fun empty(id: String) = NetworkChapter(
            id = id,
            description = null,
            titles = null,
            canonicalTitle = null,
            number = null,
            volumeNumber = null,
            length = null,
            thumbnail = null,
            published = null
        )
    }
}
