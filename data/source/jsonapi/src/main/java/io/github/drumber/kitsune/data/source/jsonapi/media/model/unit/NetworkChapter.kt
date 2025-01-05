package io.github.drumber.kitsune.data.source.jsonapi.media.model.unit

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.common.model.Image
import io.github.drumber.kitsune.data.common.model.Titles

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
