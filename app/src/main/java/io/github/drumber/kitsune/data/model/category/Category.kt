package io.github.drumber.kitsune.data.model.category

import android.os.Parcelable
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import kotlinx.parcelize.Parcelize

@Parcelize
@Type("categories")
data class Category(
    @Id val id: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val title: String?,
    val description: String?,
    val totalMediaCount: Int?,
    val slug: String?,
    val nsfw: Boolean?,
    val childCount: Int?
): Parcelable
