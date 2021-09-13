package io.github.drumber.kitsune.data.model.resource

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("episodes")
data class Episode(
    @Id val id: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val description: String?,
    val titles: Titles?,
    val canonicalTitle: String?,
    val seasonNumber: Int?,
    val number: Int?,
    val relativeNumber: Int?,
    val airdate: String?,
    val length: String?,
    val thumbnail: Image?
) : Parcelable
