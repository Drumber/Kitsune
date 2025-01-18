package io.github.drumber.kitsune.ui.library_new.composables

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.library.LibraryStatus
import io.github.drumber.kitsune.ui.theme.KitsuneTheme
import io.github.drumber.kitsune.ui.theme.LibraryStatusColors
import io.github.drumber.kitsune.ui.theme.LocalLibraryStatusColors

@Composable
fun LibraryNavigationActions(
    modifier: Modifier = Modifier,
    onActionClick: (LibraryStatus) -> Unit = {},
    onWholeLibraryClick: () -> Unit = {}
) {
    val actions = listOf(
        LibraryAction(
            icon = painterResource(R.drawable.ic_incomplete_circle_24),
            status = LibraryStatus.Current,
            labelAnime = R.string.library_status_watching,
            labelManga = R.string.library_status_reading
        ),
        LibraryAction(
            icon = painterResource(R.drawable.ic_bookmark_added_24),
            status = LibraryStatus.Planned,
            labelAnime = R.string.library_status_planned,
            labelManga = R.string.library_status_planned_manga
        ),
        LibraryAction(
            icon = painterResource(R.drawable.ic_done_24),
            status = LibraryStatus.Completed,
            labelAnime = R.string.library_status_completed
        ),
        LibraryAction(
            icon = painterResource(R.drawable.ic_watch_later_24),
            status = LibraryStatus.OnHold,
            labelAnime = R.string.library_status_on_hold
        ),
        LibraryAction(
            icon = painterResource(R.drawable.ic_cancel_presentation_24),
            status = LibraryStatus.Dropped,
            labelAnime = R.string.library_status_dropped
        )
    )

    Card(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = 8.dp)
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.anime),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.manga),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            actions.forEach { action ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LibraryActionCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        painter = action.icon,
                        colors = LocalLibraryStatusColors.current.getCardColorsFor(action.status),
                        text = stringResource(action.labelAnime),
                        onClick = { onActionClick(action.status) }
                    )
                    LibraryActionCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        painter = action.icon,
                        colors = LocalLibraryStatusColors.current.getCardColorsFor(action.status),
                        text = stringResource(action.labelManga),
                        onClick = { onActionClick(action.status) }
                    )
                }
            }

            LibraryActionCard(
                modifier = Modifier.height(IntrinsicSize.Min),
                painter = painterResource(R.drawable.ic_outline_bookmarks_24),
                text = "View your whole library",
                onClick = onWholeLibraryClick
            )
        }
    }
}

@Composable
private fun LibraryActionCard(
    modifier: Modifier = Modifier,
    painter: Painter,
    text: String,
    colors: CardColors = CardDefaults.elevatedCardColors(),
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        shape = CardDefaults.elevatedShape,
        colors = colors,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(painter, null)
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun LibraryStatusColors.getCardColorsFor(status: LibraryStatus): CardColors {
    val (containerColor, contentColor) = when (status) {
        LibraryStatus.Current -> currentContainer to onCurrentContainer
        LibraryStatus.Planned -> plannedContainer to onPlannedContainer
        LibraryStatus.Completed -> completedContainer to onCompletedContainer
        LibraryStatus.OnHold -> onHoldContainer to onOnHoldContainer
        LibraryStatus.Dropped -> droppedContainer to onDroppedContainer
    }

    return CardDefaults.elevatedCardColors(
        containerColor = containerColor.copy(alpha = 0.7f)
            .compositeOver(CardDefaults.elevatedCardColors().containerColor),
        contentColor = contentColor
    )
}

private data class LibraryAction(
    val icon: Painter,
    val status: LibraryStatus,
    @StringRes val labelAnime: Int,
    @StringRes val labelManga: Int = labelAnime
)

@Preview(showBackground = true)
@Composable
private fun LibraryNavigationActionsPreview() {
    KitsuneTheme(darkTheme = false) {
        LibraryNavigationActions()
    }
}