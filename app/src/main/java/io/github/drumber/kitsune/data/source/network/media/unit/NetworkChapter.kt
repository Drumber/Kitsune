package io.github.drumber.kitsune.data.source.network.media.unit

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.common.Image
import io.github.drumber.kitsune.data.common.Titles

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

    override val thumbnail: Image?,
    val published: String?
) : NetworkMediaUnit
