package io.github.drumber.kitsune.data.model.resource.manga

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import io.github.drumber.kitsune.data.model.resource.Image
import io.github.drumber.kitsune.data.model.resource.Titles
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("chapters")
data class Chapter(
    @Id val id: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val description: String?,
    val titles: Titles?,
    val canonicalTitle: String?,
    val volumeNumber: Int?,
    val number: Int?,
    val published: String?,
    val length: String?,
    val thumbnail: Image?
) : Parcelable
