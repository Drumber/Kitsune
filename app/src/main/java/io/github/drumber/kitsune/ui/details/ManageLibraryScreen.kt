package io.github.drumber.kitsune.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus

@Composable
fun ManageLibraryScreen(
    title: String?,
    isAnime: Boolean,
    existsInLibrary: Boolean,
    onStatusClick: (LibraryStatus) -> Unit,
    onRemoveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        if (!title.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }
        LibraryStatusButton(
            text = if (isAnime) {
                stringResource(R.string.library_status_watching)
            } else {
                stringResource(R.string.library_status_reading)
            },
            onClick = { onStatusClick(LibraryStatus.Current) }
        )
        LibraryStatusButton(
            text = if (isAnime) {
                stringResource(R.string.library_status_planned)
            } else {
                stringResource(R.string.library_status_planned_manga)
            },
            onClick = { onStatusClick(LibraryStatus.Planned) }
        )
        LibraryStatusButton(
            text = stringResource(R.string.library_status_completed),
            onClick = { onStatusClick(LibraryStatus.Completed) }
        )
        LibraryStatusButton(
            text = stringResource(R.string.library_status_on_hold),
            onClick = { onStatusClick(LibraryStatus.OnHold) }
        )
        LibraryStatusButton(
            text = stringResource(R.string.library_status_dropped),
            onClick = { onStatusClick(LibraryStatus.Dropped) }
        )
        if (existsInLibrary) {
            LibraryStatusButton(
                text = stringResource(R.string.library_action_remove),
                onClick = onRemoveClick
            )
        }
    }
}

@Composable
private fun LibraryStatusButton(text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
    }
}
