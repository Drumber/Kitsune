package io.github.drumber.kitsune.ui.component.compose.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.ui.KitsuneTestTags
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

/**
 * A horizontal carousel section with a clickable header title and a [LazyRow] of items.
 * Replaces the XML-based [io.github.drumber.kitsune.ui.component.ExploreSection].
 *
 * - [items] = null → shows a loading spinner in place of the row
 * - [items] = empty list → shows nothing (callers decide whether to hide the section)
 * - [onHeaderClick] = null → the header arrow is hidden and the row is not clickable
 *
 * Example:
 * ```
 * ExploreSection(
 *     title = "Trending This Week",
 *     items = trendingAnime,
 *     onHeaderClick = { navController.navigate(Route.TrendingAnime) },
 *     contentPadding = PaddingValues(horizontal = 16.dp)
 * ) { media ->
 *     MediaItemCard(media = media, onClick = { onItemClick(media) })
 * }
 * ```
 */
@Composable
fun <T : Any> ExploreSection(
    title: String,
    items: List<T>?,
    modifier: Modifier = Modifier,
    onHeaderClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    itemMinHeight: Dp = 0.dp,
    itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ExploreSectionHeader(title = title, onHeaderClick = onHeaderClick)
        when {
            items == null -> ExploreSectionLoading(minHeight = itemMinHeight)
            items.isEmpty() -> Unit
            else -> LazyRow(
                contentPadding = contentPadding,
                horizontalArrangement = horizontalArrangement
            ) {
                items(items) { item ->
                    itemContent(item)
                }
            }
        }
    }
}

@Composable
private fun ExploreSectionHeader(
    title: String,
    onHeaderClick: (() -> Unit)?
) {
    val clickableModifier = if (onHeaderClick != null) {
        Modifier.clickable(role = Role.Button, onClick = onHeaderClick)
    } else {
        Modifier
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(KitsuneTestTags.ExploreSectionHeader)
            .then(clickableModifier)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (onHeaderClick != null) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExploreSectionLoading(minHeight: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(minHeight.coerceAtLeast(80.dp)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
    }
}

// region Previews

@Preview(showBackground = true, name = "ExploreSection — loaded")
@Composable
private fun ExploreSectionLoadedPreview() {
    KitsuneTheme {
        ExploreSection(
            title = "Trending This Week",
            items = listOf("Anime 1", "Anime 2", "Anime 3", "Anime 4"),
            onHeaderClick = {}
        ) { label ->
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 140.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = label, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview(showBackground = true, name = "ExploreSection — loading")
@Composable
private fun ExploreSectionLoadingPreview() {
    KitsuneTheme {
        ExploreSection<String>(
            title = "Popular Manga",
            items = null,
            onHeaderClick = {}
        ) {}
    }
}

@Preview(showBackground = true, name = "ExploreSection — no header click")
@Composable
private fun ExploreSectionNoClickPreview() {
    KitsuneTheme {
        ExploreSection(
            title = "Featured",
            items = listOf("Item A", "Item B"),
            onHeaderClick = null
        ) { label ->
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 140.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = label, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// endregion
