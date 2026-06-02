package io.github.drumber.kitsune.data.presentation.model.feed

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Server-resolved embed for a link in post/comment content (Tenor GIF, YouTube video, Twitter card,
 * generic link preview).
 */
@Parcelize
data class Embed(
    val kind: String?,
    val title: String?,
    val description: String?,
    val url: String?,
    val siteName: String?,
    val imageUrl: String?,
    val videoUrl: String?,
    val videoType: String?
) : Parcelable {

    val isVideo: Boolean
        get() = kind?.startsWith("video") == true || !videoUrl.isNullOrBlank()

    val isGif: Boolean
        get() = imageUrl?.substringBefore('?')?.endsWith(".gif", ignoreCase = true) == true
}
