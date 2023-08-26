package io.github.drumber.kitsune.domain.model.infrastructure.media.category

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("categories")
data class Category(
    @Id
    val id: String,
    val slug: String?,

    val title: String?,
    val description: String?,
    val nsfw: Boolean?,

    val totalMediaCount: Int?,
    val childCount: Int?
): Parcelable
