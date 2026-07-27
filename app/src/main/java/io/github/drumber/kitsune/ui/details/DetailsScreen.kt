package io.github.drumber.kitsune.ui.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.library.getStringResId
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.media.category.Category
import io.github.drumber.kitsune.data.presentation.model.media.relationship.MediaRelationship
import io.github.drumber.kitsune.data.presentation.model.reaction.MediaReaction
import io.github.drumber.kitsune.data.presentation.model.user.Favorite
import io.github.drumber.kitsune.ui.KitsuneTestTags
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneCollapsingTopAppBar
import io.github.drumber.kitsune.ui.component.compose.media.ExpandableText
import io.github.drumber.kitsune.ui.component.compose.media.MediaCover

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UnusedParameter")
@Composable
fun DetailsScreen(
    media: Media?,
    libraryEntry: LibraryEntryWithModification?,
    favorite: Favorite?,
    reactions: List<MediaReaction>,
    isLoading: Boolean,
    isLoggedIn: Boolean,
    onNavigateUp: () -> Unit,
    onShareMedia: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenExternal: () -> Unit,
    onManageLibrary: () -> Unit,
    onEditLibraryEntry: () -> Unit,
    onNavigateToEpisodes: () -> Unit,
    onNavigateToCharacters: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToReactions: () -> Unit,
    onNavigateToCategory: (Category) -> Unit,
    onNavigateToFranchise: (Media) -> Unit,
    onUpvoteReaction: (MediaReaction) -> Unit,
    onAddReaction: () -> Unit,
    onCoverClick: () -> Unit,
    onPosterClick: () -> Unit,
    onOpenStreamingLink: (String) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            DetailsTopBar(
                media = media,
                favorite = favorite,
                collapsedFraction = scrollBehavior.state.collapsedFraction,
                onNavigateUp = onNavigateUp,
                onShare = onShareMedia,
                onToggleFavorite = onToggleFavorite,
                onOpenExternal = onOpenExternal,
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        DetailsContent(
            media = media,
            libraryEntry = libraryEntry,
            reactions = reactions,
            isLoading = isLoading,
            paddingValues = paddingValues,
            onManageLibrary = onManageLibrary,
            onEditLibraryEntry = onEditLibraryEntry,
            onNavigateToEpisodes = onNavigateToEpisodes,
            onNavigateToCharacters = onNavigateToCharacters,
            onNavigateToFeed = onNavigateToFeed,
            onNavigateToReactions = onNavigateToReactions,
            onNavigateToCategory = onNavigateToCategory,
            onNavigateToFranchise = onNavigateToFranchise,
            onUpvoteReaction = onUpvoteReaction,
            onAddReaction = onAddReaction,
            onPosterClick = onPosterClick,
            onOpenStreamingLink = onOpenStreamingLink
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsTopBar(
    media: Media?,
    favorite: Favorite?,
    collapsedFraction: Float,
    onNavigateUp: () -> Unit,
    onShare: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenExternal: () -> Unit,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior
) {
    Box {
        AsyncImage(
            model = media?.coverImageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .graphicsLayer { alpha = 1f - collapsedFraction }
        )
        KitsuneCollapsingTopAppBar(
            title = { Text(media?.title.orEmpty(), maxLines = 2, overflow = TextOverflow.Ellipsis) },
            navigationIcon = { KitsuneBackButton(onNavigateUp) },
            actions = {
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.action_share)
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (favorite != null) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = null
                    )
                }
                IconButton(onClick = onOpenExternal) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                }
            },
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                scrolledContainerColor = MaterialTheme.colorScheme.surface
            ),
            scrollBehavior = scrollBehavior
        )
    }
}

@Composable
private fun DetailsContent(
    media: Media?,
    libraryEntry: LibraryEntryWithModification?,
    reactions: List<MediaReaction>,
    isLoading: Boolean,
    paddingValues: PaddingValues,
    onManageLibrary: () -> Unit,
    onEditLibraryEntry: () -> Unit,
    onNavigateToEpisodes: () -> Unit,
    onNavigateToCharacters: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToReactions: () -> Unit,
    onNavigateToCategory: (Category) -> Unit,
    onNavigateToFranchise: (Media) -> Unit,
    onUpvoteReaction: (MediaReaction) -> Unit,
    onAddReaction: () -> Unit,
    onPosterClick: () -> Unit,
    onOpenStreamingLink: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag(KitsuneTestTags.DetailsContent)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(15.dp)
    ) {
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }
        MediaHeaderSection(
            media = media,
            libraryEntry = libraryEntry,
            onManageLibrary = onManageLibrary,
            onEditLibraryEntry = onEditLibraryEntry,
            onPosterClick = onPosterClick
        )
        if (!media?.categories.isNullOrEmpty()) {
            CategoryChipsRow(
                categories = media?.categories.orEmpty(),
                onCategoryClick = onNavigateToCategory
            )
        }
        if (!media?.description.isNullOrBlank()) {
            ExpandableText(
                text = media?.description,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(KitsuneTestTags.DetailsDescription)
                    .padding(bottom = 24.dp)
            )
        }
        NavigationButtonsSection(
            media = media,
            onNavigateToEpisodes = onNavigateToEpisodes,
            onNavigateToCharacters = onNavigateToCharacters,
            onNavigateToFeed = onNavigateToFeed
        )
        if (reactions.isNotEmpty()) {
            ReactionsSection(
                reactions = reactions,
                onUpvote = onUpvoteReaction,
                onAddReaction = onAddReaction,
                onSeeAll = onNavigateToReactions
            )
        }
        val relationships = media?.mediaRelationships?.sortedBy { it.role?.ordinal }
        if (!relationships.isNullOrEmpty()) {
            FranchiseSection(
                relationships = relationships,
                onItemClick = onNavigateToFranchise
            )
        }
        val streamingLinks = (media as? Anime)?.streamingLinks
        if (!streamingLinks.isNullOrEmpty()) {
            StreamingLinksSection(
                links = streamingLinks.map { it.url to (it.streamer?.siteName ?: it.url.orEmpty()) },
                onLinkClick = onOpenStreamingLink
            )
        }
    }
}

@Composable
private fun MediaHeaderSection(
    media: Media?,
    libraryEntry: LibraryEntryWithModification?,
    onManageLibrary: () -> Unit,
    onEditLibraryEntry: () -> Unit,
    onPosterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        AsyncImage(
            model = media?.posterImageUrl,
            contentDescription = media?.title,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_insert_photo_48),
            error = painterResource(R.drawable.ic_insert_photo_48),
            modifier = Modifier
                .size(width = 106.dp, height = 150.dp)
                .clickable(onClick = onPosterClick)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            val isManga = media !is Anime
            val libraryBtnText = libraryEntry?.status?.getStringResId(!isManga)
                ?: R.string.library_action_add
            Button(
                onClick = onManageLibrary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(libraryBtnText))
            }
            if (libraryEntry != null) {
                OutlinedButton(
                    onClick = onEditLibraryEntry,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.library_action_edit))
                }
            }
        }
    }
}

@Composable
private fun CategoryChipsRow(categories: List<Category>, onCategoryClick: (Category) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(KitsuneTestTags.DetailsCategories)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.sortedBy { it.title }.take(8).forEach { category ->
            androidx.compose.material3.FilterChip(
                selected = false,
                onClick = { onCategoryClick(category) },
                label = { Text(category.title.orEmpty(), maxLines = 1) }
            )
        }
    }
}

@Composable
private fun NavigationButtonsSection(
    media: Media?,
    onNavigateToEpisodes: () -> Unit,
    onNavigateToCharacters: () -> Unit,
    onNavigateToFeed: () -> Unit
) {
    val episodeBtnText = if (media is Anime) R.string.title_episodes else R.string.title_chapters
    OutlinedButton(
        onClick = onNavigateToEpisodes,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(KitsuneTestTags.DetailsEpisodesButton)
    ) {
        Text(stringResource(episodeBtnText))
    }
    Spacer(Modifier.height(8.dp))
    OutlinedButton(
        onClick = onNavigateToCharacters,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(KitsuneTestTags.DetailsCharactersButton)
    ) {
        Text(stringResource(R.string.title_characters))
    }
    Spacer(Modifier.height(8.dp))
    OutlinedButton(onClick = onNavigateToFeed, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.title_posts))
    }
}

@Composable
private fun ReactionsSection(
    reactions: List<MediaReaction>,
    onUpvote: (MediaReaction) -> Unit,
    onAddReaction: () -> Unit,
    onSeeAll: () -> Unit
) {
    Spacer(Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.title_reactions),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onAddReaction) {
            Icon(Icons.Default.MoreVert, contentDescription = null)
        }
        IconButton(onClick = onSeeAll) {
            Text(stringResource(R.string.action_see_all), style = MaterialTheme.typography.labelSmall)
        }
    }
    LazyRow(modifier = Modifier.fillMaxWidth()) {
        items(reactions, key = { it.id }) { reaction ->
            ReactionPreviewCard(reaction = reaction, onUpvote = { onUpvote(reaction) })
        }
    }
}

@Composable
@Suppress("UnusedParameter")
private fun ReactionPreviewCard(reaction: MediaReaction, onUpvote: () -> Unit) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .padding(end = 8.dp)
    ) {
        val content = reaction.reaction?.takeIf { it.isNotBlank() } ?: reaction.content
        Text(
            text = content.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = reaction.authorName.orEmpty(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FranchiseSection(
    relationships: List<MediaRelationship>,
    onItemClick: (Media) -> Unit
) {
    Spacer(Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.title_more_franchise),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    LazyRow(modifier = Modifier.fillMaxWidth()) {
        items(relationships, key = { it.id }) { rel ->
            rel.media?.let { media ->
                MediaCover(
                    imageUrl = media.posterImageUrl,
                    contentDescription = media.title,
                    modifier = Modifier
                        .size(width = 106.dp, height = 150.dp)
                        .clickable { onItemClick(media) }
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun StreamingLinksSection(
    links: List<Pair<String?, String>>,
    onLinkClick: (String) -> Unit
) {
    Spacer(Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.title_streamer),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    LazyRow(modifier = Modifier.fillMaxWidth()) {
        items(links, key = { it.second }) { (url, name) ->
            OutlinedButton(
                onClick = { url?.let { onLinkClick(it) } },
                enabled = url != null,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(name, maxLines = 1)
            }
        }
    }
}
