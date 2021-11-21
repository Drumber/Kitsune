package io.github.drumber.kitsune.data.model.resource

import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("chapters")
data class Chapter(
    @Id override val id: String?,
    val createdAt: String?,
    val updatedAt: String?,
    override val description: String?,
    override val titles: Titles?,
    override val canonicalTitle: String?,
    val volumeNumber: Int?,
    override val number: Int?,
    val published: String?,
    override val length: String?,
    override val thumbnail: Image?
) : MediaUnit
