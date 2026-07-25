package io.github.drumber.kitsune.ui.createpost

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.algolia.instantsearch.core.searchbox.SearchBoxView
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.ui.component.compose.list.PagingGrid
import io.github.drumber.kitsune.ui.component.compose.media.MediaItemCard

@Composable
fun MediaPickerScreen(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchItems: LazyPagingItems<Media>,
    status: MediaPickerViewModel.Status?,
    onMediaClick: (Media) -> Unit
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.groups_search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.large
        )
        when (status) {
            MediaPickerViewModel.Status.NotInitialized ->
                MediaPickerLoadingContent(modifier = Modifier.fillMaxSize())

            MediaPickerViewModel.Status.NotAvailable ->
                MediaPickerStatusText(
                    text = stringResource(R.string.search_provider_not_available),
                    modifier = Modifier.fillMaxSize()
                )

            MediaPickerViewModel.Status.Error ->
                MediaPickerStatusText(
                    text = stringResource(R.string.search_provider_error),
                    modifier = Modifier.fillMaxSize()
                )

            else ->
                PagingGrid(
                    items = searchItems,
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    key = { it.id }
                ) { media ->
                    MediaItemCard(
                        imageUrl = media?.posterImageUrl,
                        title = media?.title,
                        onClick = { media?.let(onMediaClick) },
                        modifier = Modifier.size(width = 106.dp, height = 150.dp)
                    )
                }
        }
    }
}

@Composable
private fun MediaPickerLoadingContent(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier,
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MediaPickerStatusText(text: String, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * A [SearchBoxView] implementation that bridges the Algolia InstantSearch search box
 * connector to Compose state. The connector calls [setText] to push the current query,
 * and the composable calls [change] to push user input back to the connector.
 */
internal class ComposeSearchBoxView : SearchBoxView {
    private val _query = mutableStateOf("")
    val query: androidx.compose.runtime.State<String> = _query

    override var onQueryChanged: ((String?) -> Unit)? = null
    override var onQuerySubmitted: ((String?) -> Unit)? = null

    override fun setText(text: String?, submitQuery: Boolean) {
        _query.value = text.orEmpty()
    }

    fun change(text: String) {
        _query.value = text
        onQueryChanged?.invoke(text)
    }
}
