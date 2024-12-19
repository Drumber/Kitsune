package io.github.drumber.kitsune.ui.library_new

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModificationAndNextUnit
import io.github.drumber.kitsune.ui.library_new.composables.LibraryEntryWithNextUnitItem
import io.github.drumber.kitsune.ui.library_new.composables.toLibraryEntryWithNextUnitData

@Composable
fun LibraryContent(
    modifier: Modifier = Modifier,
    currentLibraryEntries: List<LibraryEntryWithModificationAndNextUnit> = emptyList()
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .then(modifier)
    ) {
        CurrentLibraryEntriesShelf(Modifier.fillMaxWidth(), currentLibraryEntries)
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
    libraryEntries: List<LibraryEntryWithModificationAndNextUnit> = emptyList(),
    onDecrementProgress: (LibraryEntryWithModification) -> Unit = {},
    onIncrementProgress: (LibraryEntryWithModification) -> Unit = {},
    onRatingClick: (LibraryEntryWithModification) -> Unit = {}
) {
    LazyRow(modifier) {
        items(libraryEntries) {
            LibraryEntryWithNextUnitItem(
                data = it.toLibraryEntryWithNextUnitData(LocalContext.current),
                onDecrementProgress = { onDecrementProgress(it.libraryEntryWithModification) },
                onIncrementProgress = { onIncrementProgress(it.libraryEntryWithModification) },
                onRatingClick = { onRatingClick(it.libraryEntryWithModification) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LibraryContentPreview() {
    LibraryContent()
}
