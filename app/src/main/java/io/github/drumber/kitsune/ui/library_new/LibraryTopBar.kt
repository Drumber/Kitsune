package io.github.drumber.kitsune.ui.library_new

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.data.presentation.extension.title
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
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
    onSearchQueryChange: (String) -> Unit = { },
    onSearchSubmit: (String) -> Unit = { },
    onSearchToggle: (Boolean) -> Unit = { },
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets
) {
    val topAppBarColors = TopAppBarDefaults.largeTopAppBarColors()
    val colorTransitionFraction = scrollBehavior.state.collapsedFraction
    val appBarContainerColor = with(topAppBarColors) {
        lerp(
            containerColor,
            scrolledContainerColor,
            FastOutLinearInEasing.transform(colorTransitionFraction)
        )
    }

    val isDraggableEnabled by remember {
        derivedStateOf {
            with(scrollBehavior.state) {
                heightOffsetLimit - heightOffset != 0f || contentOffset == 0f
            }
        }
    }

    val dragModifier = Modifier.draggable(
        orientation = Orientation.Vertical,
        state = rememberDraggableState { delta ->
            scrollBehavior.nestedScrollConnection.onPostScroll(
                Offset(0f, delta),
                Offset(0f, delta),
                NestedScrollSource.UserInput
            )
        },
        onDragStopped = { velocity ->
            scrollBehavior.nestedScrollConnection.onPostFling(Velocity.Zero, Velocity(0f, velocity))
        },
        enabled = !state.isSearching && isDraggableEnabled
    )

    val heightOffsetAnimation = remember(state.isSearching) {
        Animatable(scrollBehavior.state.heightOffset)
    }
    LaunchedEffect(state.isSearching) {
        if (state.isSearching) {
            heightOffsetAnimation.animateTo(scrollBehavior.state.heightOffsetLimit, tween()) {
                scrollBehavior.state.heightOffset = value
            }
        }
    }

    Surface(
        modifier = modifier,
        color = appBarContainerColor
    ) {
        Column(Modifier.fillMaxWidth()) {
            LargeTopAppBar(
                title = { Text("Library") },
                colors = topAppBarColors.copy(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior,
                collapsedHeight = 0.dp,
                windowInsets = windowInsets
            )

            SearchBar(
                modifier = Modifier
                    .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Horizontal))
                    .then(dragModifier)
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = state.query,
                        onQueryChange = onSearchQueryChange,
                        onSearch = onSearchSubmit,
                        expanded = state.isSearching,
                        onExpandedChange = onSearchToggle,
                        placeholder = { Text("Search in your library") },
                        leadingIcon = {
                            if (state.isSearching) {
                                IconButton(
                                    onClick = { onSearchToggle(false) }
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Default.ArrowBack,
                                        contentDescription = null
                                    )
                                }
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null)
                            }
                        }
                    )
                },
                expanded = state.isSearching,
                onExpandedChange = onSearchToggle,
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LibraryTopBarPreview() {
    LibraryTopBar()
}