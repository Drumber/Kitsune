package io.github.drumber.kitsune.data.presentation.model.feed

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    val id: String,
    val createdAt: String?,

    val content: String?,
    val contentFormatted: String?,

    val spoiler: Boolean,
    val nsfw: Boolean,

    val commentsCount: Int,
    val likesCount: Int,

    val authorName: String?,
    val authorAvatarUrl: String?,

    val mediaTitle: String?,
    val mediaPosterUrl: String?,
    val mediaSynopsis: String?,
    val mediaSlug: String?,
    val mediaIsAnime: Boolean?,

    val spoiledUnitNumber: Int?,
    val spoiledUnitTitle: String?,
    val spoiledUnitIsEpisode: Boolean,

    val imageUrls: List<String>,

    val embed: Embed?
) : Parcelable
