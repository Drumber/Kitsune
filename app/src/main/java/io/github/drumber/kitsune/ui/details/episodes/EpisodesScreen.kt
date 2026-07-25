package io.github.drumber.kitsune.ui.details.episodes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.media.unit.MediaUnit
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneCollapsingTopAppBar
import io.github.drumber.kitsune.ui.component.compose.list.PagingColumn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodesScreen(
    title: String,
    items: LazyPagingItems<MediaUnit>,
    posterUrl: String?,
    isWatchCheckboxEnabled: Boolean,
    numberWatched: Int,
    onNavigateUp: () -> Unit,
    onItemClick: (MediaUnit) -> Unit,
    onWatchedChanged: (MediaUnit, Boolean) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            KitsuneCollapsingTopAppBar(
                title = { Text(title) },
                navigationIcon = { KitsuneBackButton(onNavigateUp) },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        PagingColumn(
            items = items,
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues
        ) { item ->
            if (item != null) {
                EpisodeItem(
                    item = item,
                    posterUrl = posterUrl,
                    isWatchCheckboxEnabled = isWatchCheckboxEnabled,
                    isWatched = (item.number ?: 0) <= numberWatched,
                    onClick = { onItemClick(item) },
                    onWatchedChanged = { isWatched -> onWatchedChanged(item, isWatched) }
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Suppress("UnusedParameter")
@Composable
private fun EpisodeItem(
    item: MediaUnit,
    posterUrl: String?,
    isWatchCheckboxEnabled: Boolean,
    isWatched: Boolean,
    onClick: () -> Unit,
    onWatchedChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideImage(
            model = item.thumbnail?.originalOrDown() ?: posterUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(96.dp)
                .aspectRatio(16f / 9f)
        ) {
            it.placeholder(R.drawable.ic_insert_photo_48).error(R.drawable.ic_insert_photo_48)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title(context).orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (item.hasValidTitle()) {
                Text(
                    text = item.numberText(context).orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (isWatchCheckboxEnabled) {
            Checkbox(checked = isWatched, onCheckedChange = onWatchedChanged)
        }
    }
}
