package io.github.drumber.kitsune.ui.library_new.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.github.drumber.kitsune.R

@Composable
fun LibraryEntryProgressAction(
    hasStartedConsuming: Boolean,
    canProgress: Boolean,
    progress: Int,
    unitCountFormatted: String?,
    onMinusClick: () -> Unit,
    onPlusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (hasStartedConsuming) "$progress/$unitCountFormatted"
            else stringResource(R.string.library_not_started)
        )
        Spacer(Modifier.weight(1f))
        OutlinedIconButton(
            onClick = onMinusClick,
            shape = MaterialTheme.shapes.small,
            enabled = hasStartedConsuming
        ) {
            Icon(
                painterResource(R.drawable.ic_remove_24),
                contentDescription = stringResource(R.string.hint_mark_watched)
            )
        }
        FilledIconButton(
            onClick = onPlusClick,
            shape = MaterialTheme.shapes.small,
            enabled = canProgress
        ) {
            Icon(
                painterResource(R.drawable.ic_add_24),
                contentDescription = stringResource(R.string.hint_mark_watched)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
private fun LibraryEntryProgressActionPreview() {
    LibraryEntryProgressAction(
        hasStartedConsuming = true,
        canProgress = true,
        progress = 1,
        unitCountFormatted = "12",
        onMinusClick = {},
        onPlusClick = {}
    )
}

@Preview(showBackground = true, widthDp = 200)
@Composable
private fun LibraryEntryProgressActionNotStartedPreview() {
    LibraryEntryProgressAction(
        hasStartedConsuming = false,
        canProgress = true,
        progress = 0,
        unitCountFormatted = "12",
        onMinusClick = {},
        onPlusClick = {}
    )
}

@Preview(showBackground = true, widthDp = 200)
@Composable
private fun LibraryEntryProgressActionFinishedPreview() {
    LibraryEntryProgressAction(
        hasStartedConsuming = true,
        canProgress = false,
        progress = 12,
        unitCountFormatted = "12",
        onMinusClick = {},
        onPlusClick = {}
    )
}