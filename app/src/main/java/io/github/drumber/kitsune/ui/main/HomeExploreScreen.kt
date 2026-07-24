package io.github.drumber.kitsune.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.ui.component.compose.list.ExploreSection
import io.github.drumber.kitsune.ui.component.compose.media.MediaItemCard
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import io.github.drumber.kitsune.util.network.ResponseData

@Composable
fun HomeExploreScreen(
    modifier: Modifier = Modifier,
    sections: List<HomeExploreSectionUiState>,
    onItemClick: (Media) -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        sections.forEachIndexed { index, section ->
            if (index > 0) {
                // matches the transparent 20dp explore_section_divider used by the XML layout
                Spacer(modifier = Modifier.height(20.dp))
            }
            HomeExploreSectionItem(
                section = section,
                onItemClick = onItemClick,
                onRetry = onRetry
            )
        }
    }
}

@Composable
private fun HomeExploreSectionItem(
    section: HomeExploreSectionUiState,
    onItemClick: (Media) -> Unit,
    onRetry: () -> Unit
) {
    when (val resp = section.response) {
        null -> ExploreSection<Media>(
            title = section.title,
            items = null,
            onHeaderClick = section.onHeaderClick,
            itemMinHeight = 150.dp
        ) {}
        is ResponseData.Success -> ExploreSection(
            title = section.title,
            items = resp.data,
            onHeaderClick = section.onHeaderClick
        ) { media ->
            MediaItemCard(
                modifier = Modifier.size(width = 106.dp, height = 150.dp),
                imageUrl = media.posterImageUrl,
                title = media.title,
                subtypeLabel = media.subtypeFormatted.ifBlank { null },
                onClick = { onItemClick(media) }
            )
        }
        is ResponseData.Error -> ExploreSectionError(
            title = section.title,
            onRetry = onRetry
        )
    }
}

@Composable
private fun ExploreSectionError(
    title: String,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(10.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.error_resource_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.action_retry))
                }
            }
        }
    }
}

private fun previewAnime(title: String): Media = Anime(
    id = title,
    slug = null,
    description = null,
    titles = null,
    canonicalTitle = title,
    abbreviatedTitles = null,
    averageRating = null,
    ratingFrequencies = null,
    userCount = null,
    favoritesCount = null,
    popularityRank = null,
    ratingRank = null,
    startDate = null,
    endDate = null,
    nextRelease = null,
    tba = null,
    status = null,
    ageRating = null,
    ageRatingGuide = null,
    nsfw = null,
    posterImage = null,
    coverImage = null,
    totalLength = null,
    episodeCount = null,
    episodeLength = null,
    youtubeVideoId = null,
    subtype = null,
    categories = null,
    animeProduction = null,
    streamingLinks = null,
    mediaRelationships = null
)

// region Previews

@Preview(showBackground = true, name = "HomeExploreScreen — loading")
@Composable
private fun HomeExploreScreenLoadingPreview() {
    KitsuneTheme {
        HomeExploreScreen(
            sections = listOf(
                HomeExploreSectionUiState("Trending This Week", null) {},
                HomeExploreSectionUiState("Top Airing Anime", null) {}
            ),
            onItemClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "HomeExploreScreen — loaded")
@Composable
private fun HomeExploreScreenLoadedPreview() {
    KitsuneTheme {
        HomeExploreScreen(
            sections = listOf(
                HomeExploreSectionUiState(
                    title = "Trending This Week",
                    response = ResponseData.Success(
                        listOf(
                            previewAnime("Cowboy Bebop"),
                            previewAnime("Neon Genesis Evangelion"),
                            previewAnime("Fullmetal Alchemist: Brotherhood")
                        )
                    ),
                    onHeaderClick = {}
                ),
                HomeExploreSectionUiState(
                    title = "Top Airing Anime",
                    response = ResponseData.Success(emptyList()),
                    onHeaderClick = {}
                )
            ),
            onItemClick = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "HomeExploreScreen — error")
@Composable
private fun HomeExploreScreenErrorPreview() {
    KitsuneTheme {
        HomeExploreScreen(
            sections = listOf(
                HomeExploreSectionUiState(
                    title = "Trending This Week",
                    response = ResponseData.Error(Exception("Network error")),
                    onHeaderClick = {}
                ),
                HomeExploreSectionUiState("Top Airing Anime", null) {}
            ),
            onItemClick = {},
            onRetry = {}
        )
    }
}

// endregion
