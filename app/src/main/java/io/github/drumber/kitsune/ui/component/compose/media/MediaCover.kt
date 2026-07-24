package io.github.drumber.kitsune.ui.component.compose.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

/**
 * Loads a media poster image (or cover image) using GlideImage.
 *
 * Replaces the [ImageView] + Glide setup in [MediaViewHolder]/[MediaRecyclerViewAdapter].
 * Mirrors the same placeholder ([R.drawable.ic_insert_photo_48]) and crossfade used in the
 * existing adapters.
 *
 * @param imageUrl URL of the poster/cover to load. Null or blank shows the placeholder.
 * @param contentDescription Accessibility description for the image.
 */
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MediaCover(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNullOrBlank()) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
        } else {
            GlideImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            ) {
                it.placeholder(R.drawable.ic_insert_photo_48)
                    .error(R.drawable.ic_insert_photo_48)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaCoverLoadedPreview() {
    KitsuneTheme {
        MediaCover(
            modifier = Modifier
                .size(width = 106.dp, height = 150.dp),
            imageUrl = "https://media.kitsu.app/anime/poster_images/1/small.jpg"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaCoverNullUrlPreview() {
    KitsuneTheme {
        MediaCover(
            modifier = Modifier
                .size(width = 106.dp, height = 150.dp),
            imageUrl = null
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaCoverAspectRatioPreview() {
    KitsuneTheme {
        MediaCover(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .aspectRatio(106f / 150f),
            imageUrl = null
        )
    }
}
