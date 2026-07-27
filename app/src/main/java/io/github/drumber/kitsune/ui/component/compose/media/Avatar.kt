package io.github.drumber.kitsune.ui.component.compose.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

/**
 * A circular avatar image backed by Coil's [AsyncImage].
 *
 * Replaces all usages of `de.hdodenhof:circleimageview` (`CircleImageView`) across the app
 * and the `AsyncImage + clip(CircleShape)` pattern already used in the Compose-based [LoginPage].
 *
 * @param imageUrl URL of the avatar to load; null or blank shows a person icon placeholder.
 * @param size     Diameter of the circle.
 * @param contentDescription Accessibility label.
 */
@Composable
fun Avatar(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    size: Dp = 48.dp,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // The person icon stays underneath so it acts as placeholder/error state
        // while the remote avatar is loading or fails to load.
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(size * 0.6f)
        )
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarLoadedPreview() {
    KitsuneTheme {
        Avatar(
            imageUrl = "https://media.kitsu.app/users/avatars/1/large.jpg",
            size = 72.dp,
            contentDescription = "User avatar"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarNullUrlPreview() {
    KitsuneTheme {
        Avatar(
            imageUrl = null,
            size = 72.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarSmallPreview() {
    KitsuneTheme {
        Avatar(
            imageUrl = null,
            size = 40.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarLargePreview() {
    KitsuneTheme {
        Avatar(
            imageUrl = null,
            size = 100.dp
        )
    }
}
