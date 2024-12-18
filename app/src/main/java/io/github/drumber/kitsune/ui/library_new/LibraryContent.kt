package io.github.drumber.kitsune.ui.library_new

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntry
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModificationAndNextUnit
import io.github.drumber.kitsune.ui.composables.SmallPoster

@Composable
fun LibraryContent(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .then(modifier)
    ) {
        CurrentLibraryEntriesShelf()
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
    libraryEntries: List<LibraryEntryWithModificationAndNextUnit> = emptyList()
) {
    LazyRow(modifier) {
        items(libraryEntries) {
            LibraryEntryWithNextUnitItem(libraryEntryWrapper = it)
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun LibraryEntryWithNextUnitItem(
    modifier: Modifier = Modifier,
    libraryEntryWrapper: LibraryEntryWithModificationAndNextUnit
) {
    val libraryEntry = libraryEntryWrapper.libraryEntryWithModification
    val nextUnit = libraryEntryWrapper.nextUnit
    val media = libraryEntry.libraryEntry.media

    val coverImage = libraryEntryWrapper.nextUnit?.thumbnail?.originalOrDown()
        ?: media?.coverImageUrl
    val nextUnitText = when (nextUnit != null && nextUnit.hasValidTitle()) {
        true -> nextUnit.title(LocalContext.current)
        false -> nextUnit?.numberText(LocalContext.current)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = (libraryEntry.progress ?: 0) / (libraryEntry.episodeCount ?: 1).toFloat(),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "animatedLibraryProgress"
    )

    val overlayGradient = listOf(
        CardDefaults.cardColors().containerColor.copy(alpha = 0.75f),
        CardDefaults.cardColors().containerColor
    )

    Card(modifier) {
        Box {
            GlideImage(coverImage, contentDescription = null, modifier = Modifier.fillMaxSize())
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(overlayGradient)),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SmallPoster(media?.posterImageUrl, elevated = true)
                    Spacer(Modifier.width(16.dp))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.Bottom)
                    ) {
                        Text(
                            media?.title ?: stringResource(R.string.no_information),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        if (nextUnitText != null) {
                            Text(
                                nextUnitText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        LibraryEntryProgressAction(libraryEntry)
                    }
                }
                if (libraryEntry.hasEpisodesCount) {
                    LinearProgressIndicator(
                        progress = { animatedProgress }
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryEntryProgressAction(libraryEntry: LibraryEntryWithModification) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (libraryEntry.hasStartedWatching)
                (libraryEntry.progress
                    ?: 0).toString() + "/" + libraryEntry.episodeCountFormatted
            else stringResource(R.string.library_not_started)
        )
        Spacer(Modifier.weight(1f))
        OutlinedIconButton(
            onClick = { /* TODO */ },
            shape = MaterialTheme.shapes.small
        ) {
            Icon(
                painterResource(R.drawable.ic_remove_24),
                contentDescription = stringResource(R.string.hint_mark_watched)
            )
        }
        FilledIconButton(
            onClick = { /* TODO */ },
            shape = MaterialTheme.shapes.small
        ) {
            Icon(
                painterResource(R.drawable.ic_add_24),
                contentDescription = stringResource(R.string.hint_mark_watched)
            )
        }
    }
}

@Preview
@Composable
fun LibraryContentPreview() {
    LibraryContent()
}

@Preview(heightDp = 200)
@Composable
fun LibraryEntryWithNextUnitItemPreview() {
    LibraryEntryWithNextUnitItem(
        libraryEntryWrapper = LibraryEntryWithModificationAndNextUnit(
            LibraryEntryWithModification(
                LibraryEntry(
                    id = "1",
                    media = null,
                    progress = 0,
                    ratingTwenty = 0,
                    status = null,
                    reconsumeCount = 0,
                    privateEntry = false,
                    reactionSkipped = null,
                    notes = null,
                    volumesOwned = 0,
                    updatedAt = null,
                    startedAt = null,
                    finishedAt = null,
                    progressedAt = null,
                    reconsuming = false
                ), null
            ), null
        )
    )
}
