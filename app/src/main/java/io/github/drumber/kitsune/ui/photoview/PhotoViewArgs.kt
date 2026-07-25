package io.github.drumber.kitsune.ui.photoview

import android.content.Context
import android.content.Intent

/**
 * Arguments of [PhotoViewActivity]. The activity used to be a Navigation Component destination
 * with generated SafeArgs; with Navigation Compose it is started with a plain intent instead.
 */
data class PhotoViewArgs(
    val imageUrl: String,
    val title: String? = null,
    val thumbnailUrl: String? = null,
    val transitionName: String? = null
) {

    fun toIntent(context: Context) = Intent(context, PhotoViewActivity::class.java).apply {
        putExtra(EXTRA_IMAGE_URL, imageUrl)
        putExtra(EXTRA_TITLE, title)
        putExtra(EXTRA_THUMBNAIL_URL, thumbnailUrl)
        putExtra(EXTRA_TRANSITION_NAME, transitionName)
    }

    companion object {
        private const val EXTRA_IMAGE_URL = "imageUrl"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_THUMBNAIL_URL = "thumbnailUrl"
        private const val EXTRA_TRANSITION_NAME = "transitionName"

        fun fromIntent(intent: Intent) = PhotoViewArgs(
            imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL).orEmpty(),
            title = intent.getStringExtra(EXTRA_TITLE),
            thumbnailUrl = intent.getStringExtra(EXTRA_THUMBNAIL_URL),
            transitionName = intent.getStringExtra(EXTRA_TRANSITION_NAME)
        )
    }
}

/** Opens the full screen photo viewer. */
fun Context.openPhotoView(
    imageUrl: String,
    title: String? = null,
    thumbnailUrl: String? = null
) = startActivity(PhotoViewArgs(imageUrl, title, thumbnailUrl).toIntent(this))
