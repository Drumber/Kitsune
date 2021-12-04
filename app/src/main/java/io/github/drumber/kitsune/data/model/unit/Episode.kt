package io.github.drumber.kitsune.data.model.unit

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.model.media.Image
import io.github.drumber.kitsune.data.model.media.Titles
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("episodes")
data class Episode(
    @Id override val id: String?,
    val createdAt: String?,
    val updatedAt: String?,
    override val description: String?,
    override val titles: Titles?,
    override val canonicalTitle: String?,
    val seasonNumber: Int?,
    override val number: Int?,
    val relativeNumber: Int?,
    val airdate: String?,
    override val length: String?,
    override val thumbnail: Image?
) : MediaUnit
