package io.github.drumber.kitsune.ui.library_new

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModificationAndNextUnit
import io.github.drumber.kitsune.ui.library_new.composables.LibraryEntryWithNextUnitItem
import io.github.drumber.kitsune.ui.library_new.composables.toLibraryEntryWithNextUnitData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun LibraryContent(
    modifier: Modifier = Modifier,
    currentLibraryEntries: Flow<PagingData<LibraryEntryWithModificationAndNextUnit>> = emptyFlow(),
    onItemClick: (LibraryEntryWithModification) -> Unit = {},
    onIncrementProgress: (LibraryEntryWithModification) -> Unit = {},
    onRatingClick: (LibraryEntryWithModification) -> Unit = {},
    onEditClick: (LibraryEntryWithModification) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .then(modifier)
    ) {
        CurrentLibraryEntriesShelf(
            modifier = Modifier.fillMaxWidth(),
            libraryEntries = currentLibraryEntries,
            onItemClick = onItemClick,
            onIncrementProgress = onIncrementProgress,
            onRatingClick = onRatingClick,
            onEditClick = onEditClick
        )
        Spacer(Modifier.height(16.dp))
        // upcoming media calendar/list
        // want to watch shelf
        // recently watched shelf
        // links to on-hold, dropped
    }
}

@Composable
private fun CurrentLibraryEntriesShelf(
    modifier: Modifier = Modifier,
    libraryEntries: Flow<PagingData<LibraryEntryWithModificationAndNextUnit>> = emptyFlow(),
    onItemClick: (LibraryEntryWithModification) -> Unit = {},
    onIncrementProgress: (LibraryEntryWithModification) -> Unit = {},
    onRatingClick: (LibraryEntryWithModification) -> Unit = {},
    onEditClick: (LibraryEntryWithModification) -> Unit = {},
) {
    val lazyPagingItems = libraryEntries.collectAsLazyPagingItems()

    Box(modifier = modifier.height(230.dp)) {
        when {
            lazyPagingItems.loadState.refresh == LoadState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            lazyPagingItems.loadState.isIdle && lazyPagingItems.itemCount == 0 -> {
                Text(
                    text = stringResource(R.string.no_data_available),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            else -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    // preferred item width is 380dp,
                    // but can shrink to parent width (maxWidth from BoxWithConstraints)
                    // with a minimum of 350dp
                    val itemWidth = max(350.dp, min(380.dp, maxWidth))

                    LazyRow(modifier = Modifier.fillMaxSize()) {
                        items(
                            count = lazyPagingItems.itemCount,
                            key = lazyPagingItems.itemKey { it.libraryEntryWithModification.id },
                        ) { index ->
                            val item = lazyPagingItems[index]

                            if (item == null) {
                                // TODO: show placeholder
                                Text("Placeholder", Modifier.padding(16.dp))
                            } else {
                                LibraryEntryWithNextUnitItem(
                                    data = item.toLibraryEntryWithNextUnitData(LocalContext.current),
                                    onCardClick = { onItemClick(item.libraryEntryWithModification) },
                                    onIncrementProgress = { onIncrementProgress(item.libraryEntryWithModification) },
                                    onRatingClick = { onRatingClick(item.libraryEntryWithModification) },
                                    onEditClick = { onEditClick(item.libraryEntryWithModification) },
                                    modifier = Modifier
                                        .width(itemWidth)
                                        .fillMaxHeight()
                                        .padding(8.dp)
                                        .animateItem()
                                )
                            }
                        }

                        if (lazyPagingItems.loadState.append == LoadState.Loading) {
                            item {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LibraryContentPreview() {
    LibraryContent()
}
