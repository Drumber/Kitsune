package io.github.drumber.kitsune.ui.library_new

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.tooling.preview.Preview
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry

data class LibraryTopBarState(
    val isSearching: Boolean = false,
    val query: String = "",
    val suggestions: List<LibraryEntry> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryTopBar(
    modifier: Modifier = Modifier,
    state: LibraryTopBarState = LibraryTopBarState(),
    onSearchQueryChange: (String) -> Unit = { },
    onSearchSubmit: (String) -> Unit = { },
    onSearchToggle: (Boolean) -> Unit = { },
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets
) {
    SearchBar(
        modifier = modifier
            .windowInsetsPadding(windowInsets)
            .fillMaxWidth()
            .clipToBounds(),
        inputField = {
            SearchBarDefaults.InputField(
                query = state.query,
                onQueryChange = onSearchQueryChange,
                onSearch = onSearchSubmit,
                expanded = state.isSearching,
                onExpandedChange = onSearchToggle,
                placeholder = { Text("Search in your library") }
            )
        },
        expanded = state.isSearching,
        onExpandedChange = onSearchToggle
    ) {
        LazyColumn {
            items(state.suggestions) {
                ListItem(
                    headlineContent = { Text(it.media?.title ?: "-") },
                    supportingContent = { Text(it.media?.subtypeFormatted ?: "") }
                )
            }
        }
    }
}

@Preview
@Composable
fun LibraryTopBarPreview() {
    LibraryTopBar()
}