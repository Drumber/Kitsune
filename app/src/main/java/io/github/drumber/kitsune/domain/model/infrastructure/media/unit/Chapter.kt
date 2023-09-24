package io.github.drumber.kitsune.domain.model.infrastructure.media.unit

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.domain.model.infrastructure.image.Image
import io.github.drumber.kitsune.domain.model.common.media.Titles
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("chapters")
data class Chapter(
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
) : MediaUnit
