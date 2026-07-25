package io.github.drumber.kitsune.ui.component.compose.media

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.ui.KitsuneTestTags
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

/**
 * Full media card that shows a poster image, an optional subtype badge (e.g. "TV", "ONA"),
 * and a semi-transparent title overlay at the bottom — matching the XML layout in
 * `item_media.xml` and the custom [MediaItemCard] view.
 *
 * Sizing: callers control width/height via [modifier]; the card itself is match_parent
 * inside whatever box the caller gives it (mirrors the existing SMALL/MEDIUM/LARGE presets from
 * [MediaItemSize] which are 106×150 / 141×200 / 169×240 dp).
 *
 * Replaces the old RecyclerView media card and custom view.
 *
 * @param imageUrl     Poster image URL; null shows the placeholder.
 * @param title        Media title drawn at the bottom of the card.
 * @param subtypeLabel Optional badge text shown in the top-end corner (e.g. "TV"). Pass null to hide.
 * @param contentDescription Accessibility label for the cover image.
 * @param onClick      Click handler for the card.
 */
@Composable
fun MediaItemCard(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    title: String?,
    subtypeLabel: String? = null,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = modifier.testTag(KitsuneTestTags.MediaCard)
    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = cardModifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MediaCover(
                modifier = Modifier.fillMaxSize(),
                imageUrl = imageUrl,
                contentDescription = contentDescription
            )

            // Bottom gradient + title overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomStart)
                    .drawWithContent {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC000000)),
                                startY = 0f,
                                endY = size.height
                            )
                        )
                        drawContent()
                    }
            ) {
                Text(
                    text = title.orEmpty(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 10.dp)
                )
            }

            // Subtype badge (top-end corner)
            if (!subtypeLabel.isNullOrBlank()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    tonalElevation = 0.dp
                ) {
                    Text(
                        text = subtypeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaItemCardWithSubtypePreview() {
    KitsuneTheme {
        MediaItemCard(
            modifier = Modifier.size(width = 106.dp, height = 150.dp),
            imageUrl = null,
            title = "Cowboy Bebop",
            subtypeLabel = "TV",
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaItemCardLongTitlePreview() {
    KitsuneTheme {
        MediaItemCard(
            modifier = Modifier.size(width = 106.dp, height = 150.dp),
            imageUrl = null,
            title = "Sword Art Online: Alicization — War of Underworld Part 2",
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaItemCardNoTitlePreview() {
    KitsuneTheme {
        MediaItemCard(
            modifier = Modifier.size(width = 141.dp, height = 200.dp),
            imageUrl = null,
            title = null
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaItemCardLargePreview() {
    KitsuneTheme {
        Column {
            MediaItemCard(
                modifier = Modifier.size(width = 169.dp, height = 240.dp),
                imageUrl = null,
                title = "Attack on Titan Final Season",
                subtypeLabel = "TV",
                onClick = {}
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
