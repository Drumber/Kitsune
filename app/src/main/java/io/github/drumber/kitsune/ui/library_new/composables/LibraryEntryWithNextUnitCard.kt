package io.github.drumber.kitsune.ui.library_new.composables

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModificationAndNextUnit
import io.github.drumber.kitsune.ui.composables.SmallPoster

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LibraryEntryWithNextUnitItem(
    modifier: Modifier = Modifier,
    data: LibraryEntryWithNextUnitData,
    onCardClick: () -> Unit = {},
    onIncrementProgress: () -> Unit = {},
    onRatingClick: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    val overlayGradient = listOf(
        CardDefaults.cardColors().containerColor.copy(alpha = 0.6f),
        CardDefaults.cardColors().containerColor
    )

    Card(
        modifier = modifier.clipToBounds(),
        onClick = onCardClick
    ) {
        Box {
            GlideImage(
                data.coverImageModel,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Row(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(overlayGradient))
                    .padding(start = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                SmallPoster(data.posterImageModel, elevated = true)
                Spacer(Modifier.padding(8.dp))
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.align(Alignment.End)) {
                        TopSection(
                            hasRating = data.hasRating,
                            ratingFormatted = data.ratingFormatted,
                            onRatingClick = onRatingClick,
                            onEditClick = onEditClick
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Content(
                        data = data,
                        onIncrementProgress = onIncrementProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, end = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    data: LibraryEntryWithNextUnitData,
    onIncrementProgress: () -> Unit
) {
    val subtypeAndYear =
        listOfNotNull(data.mediaSubtypeFormatted, data.mediaPublishingYearFormatted)
            .joinToString(" • ")

    Column(modifier) {
        Text(
            data.mediaTitle ?: stringResource(R.string.no_information),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (subtypeAndYear.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                subtypeAndYear,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(8.dp))
        if (data.nextUnitTitle != null) {
            Text(
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = buildAnnotatedString {
                    if (data.nextUnitNumberFormatted != null) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                            append(data.nextUnitNumberFormatted, ": ")
                        }
                    }
                    append(data.nextUnitTitle)
                }
            )
            Spacer(Modifier.height(4.dp))
        }
        LibraryEntryProgressSingleAction(
            modifier = Modifier.fillMaxWidth(),
            hasStartedConsuming = data.hasStartedConsuming,
            canProgress = data.canConsume,
            progress = data.progress ?: 0,
            unitCountFormatted = data.unitCountFormatted,
            onPlusClick = onIncrementProgress
        )
        if (data.hasUnitCount) {
            Spacer(Modifier.height(4.dp))
            LibraryEntryProgressBar(
                progress = data.progress ?: 0,
                unitCount = data.unitCount ?: Int.MAX_VALUE,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TopSection(
    hasRating: Boolean,
    ratingFormatted: String?,
    onRatingClick: () -> Unit,
    onEditClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    LibraryEntryRatingButton(
        hasRating,
        ratingFormatted,
        onClick = onRatingClick
    )
    Box {
        IconButton(onClick = { menuExpanded = !menuExpanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = null)
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit entry") },
                leadingIcon = { Icon(painterResource(R.drawable.ic_edit_24), null) },
                onClick = {
                    menuExpanded = false
                    onEditClick()
                }
            )
            DropdownMenuItem(
                text = { Text("Pin entry") },
                leadingIcon = { Icon(painterResource(R.drawable.ic_push_pin_24), null) },
                onClick = {
                    menuExpanded = false
                    // TODO: action
                }
            )
        }
    }
}

data class LibraryEntryWithNextUnitData(
    val hasStartedConsuming: Boolean = false,
    val progress: Int? = null,
    val canConsume: Boolean = false,
    val hasUnitCount: Boolean = false,
    val unitCount: Int? = null,
    val unitCountFormatted: String? = null,
    val hasRating: Boolean = false,
    val ratingFormatted: String? = null,
    val coverImageModel: Any? = null,
    val posterImageModel: Any? = null,
    val mediaTitle: String? = null,
    val mediaSubtypeFormatted: String? = null,
    val mediaPublishingYearFormatted: String? = null,
    val nextUnitTitle: String? = null,
    val nextUnitNumberFormatted: String? = null
)

fun LibraryEntryWithModificationAndNextUnit.toLibraryEntryWithNextUnitData(context: Context) =
    LibraryEntryWithNextUnitData(
        hasStartedConsuming = libraryEntryWithModification.hasStartedWatching,
        progress = libraryEntryWithModification.progress,
        canConsume = libraryEntryWithModification.canWatchEpisode,
        hasUnitCount = libraryEntryWithModification.hasEpisodesCount,
        unitCount = libraryEntryWithModification.episodeCount,
        unitCountFormatted = libraryEntryWithModification.episodeCountFormatted,
        hasRating = libraryEntryWithModification.hasRating,
        ratingFormatted = libraryEntryWithModification.ratingFormatted,
        coverImageModel = nextUnit?.thumbnail?.originalOrDown()
            ?: libraryEntryWithModification.media?.coverImageUrl,
        posterImageModel = libraryEntryWithModification.media?.posterImageUrl,
        mediaTitle = libraryEntryWithModification.media?.title,
        mediaSubtypeFormatted = libraryEntryWithModification.media?.subtypeFormatted,
        mediaPublishingYearFormatted = libraryEntryWithModification
            .media?.publishingYearText(context),
        nextUnitTitle = when (nextUnit != null && nextUnit.hasValidTitle()) {
            true -> nextUnit.title(context)
            false -> nextUnit?.numberText(context)
        },
        nextUnitNumberFormatted = nextUnit?.numberText(context)
    )

@Preview(heightDp = 220)
@Composable
private fun LibraryEntryWithNextUnitItemPreview() {
    LibraryEntryWithNextUnitItem(
        data = LibraryEntryWithNextUnitData(
            mediaTitle = "Media Title looooooong over two lines",
            mediaSubtypeFormatted = "TV",
            mediaPublishingYearFormatted = "2017",
            nextUnitTitle = "Next unit tile with looooong text",
            nextUnitNumberFormatted = "Episode 2",
            ratingFormatted = "8.5",
            hasStartedConsuming = true,
            canConsume = true,
            hasUnitCount = true,
            progress = 1,
            unitCount = 12,
            unitCountFormatted = "12"
        )
    )
}