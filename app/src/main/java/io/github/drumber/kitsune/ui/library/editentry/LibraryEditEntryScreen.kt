package io.github.drumber.kitsune.ui.library.editentry

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.library.LibraryEntryWithModification
import io.github.drumber.kitsune.data.presentation.model.library.LibraryStatus
import io.github.drumber.kitsune.data.presentation.model.library.getStringResId
import io.github.drumber.kitsune.data.presentation.model.media.Anime
import io.github.drumber.kitsune.data.presentation.model.media.Manga
import io.github.drumber.kitsune.ui.component.compose.media.MediaCover
import io.github.drumber.kitsune.ui.compose.collectAsStateWithLifecycle
import io.github.drumber.kitsune.util.formatDate
import io.github.drumber.kitsune.util.parseUtcDate
import io.github.drumber.kitsune.util.rating.RatingSystemUtil.formatRatingTwenty
import org.koin.androidx.compose.koinViewModel

private val libraryStatusOptions = listOf(
    LibraryStatus.Current,
    LibraryStatus.Planned,
    LibraryStatus.Completed,
    LibraryStatus.OnHold,
    LibraryStatus.Dropped
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryEditEntryScreen(
    onDismiss: () -> Unit,
    onOpenStartedDatePicker: () -> Unit,
    onOpenFinishedDatePicker: () -> Unit,
    onShowRatingSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: LibraryEditEntryViewModel = koinViewModel()
    val wrapper by viewModel.libraryEntryWithModification.collectAsStateWithLifecycle()
    val hasChanges by viewModel.hasChanges.collectAsStateWithLifecycle(false)
    val loadState by viewModel.loadState.collectAsStateWithLifecycle(
        LibraryEditEntryViewModel.LoadState.NotLoading
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMsg = stringResource(R.string.error_library_update_failed)

    LaunchedEffect(loadState) {
        if (loadState == LibraryEditEntryViewModel.LoadState.Error) {
            snackbarHostState.showSnackbar(errorMsg)
        }
    }

    Scaffold(
        modifier = modifier.imePadding(),
        topBar = {
            LibraryEditTopBar(
                title = wrapper?.media?.title ?: "",
                hasChanges = hasChanges == true,
                onDismiss = onDismiss,
                onSave = { viewModel.saveChanges() },
                onRemove = { viewModel.removeLibraryEntry() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (loadState == LibraryEditEntryViewModel.LoadState.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            wrapper?.let { entry ->
                LibraryEditEntryForm(
                    entry = entry,
                    viewModel = viewModel,
                    onShowRatingSheet = onShowRatingSheet,
                    onOpenStartedDatePicker = onOpenStartedDatePicker,
                    onOpenFinishedDatePicker = onOpenFinishedDatePicker
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryEditTopBar(
    title: String,
    hasChanges: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onRemove: () -> Unit
) {
    var showRemoveDialog by remember { mutableStateOf(false) }

    if (showRemoveDialog) {
        RemoveEntryConfirmDialog(
            onConfirm = { showRemoveDialog = false; onRemove() },
            onDismiss = { showRemoveDialog = false }
        )
    }

    TopAppBar(
        title = { Text(text = title, maxLines = 1) },
        navigationIcon = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_close))
            }
        },
        actions = {
            IconButton(onClick = onSave, enabled = hasChanges) {
                Icon(Icons.Default.Save, stringResource(R.string.action_save_changes))
            }
            IconButton(onClick = { showRemoveDialog = true }) {
                Icon(Icons.Default.Delete, stringResource(R.string.library_action_remove))
            }
        }
    )
}

@Composable
private fun RemoveEntryConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_remove_from_library_title)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.action_remove)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
        }
    )
}

@Composable
private fun LibraryEditEntryForm(
    entry: LibraryEntryWithModification,
    viewModel: LibraryEditEntryViewModel,
    onShowRatingSheet: () -> Unit,
    onOpenStartedDatePicker: () -> Unit,
    onOpenFinishedDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isManga = entry.media is Manga
    val isAnime = entry.media is Anime

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LibraryEditMediaHeader(entry = entry)
        LibraryEditStatusField(
            status = entry.status,
            isAnime = isAnime,
            onStatusChange = { viewModel.updateLibraryEntry { mod -> mod.copy(status = it) } }
        )
        LibraryEditProgressField(entry = entry, viewModel = viewModel)
        if (isManga) {
            NumberInputRow(
                label = stringResource(R.string.library_edit_volumes),
                value = entry.volumesOwned ?: 0,
                max = (entry.media as? Manga)?.volumeCount,
                onValueChange = { viewModel.updateLibraryEntry { mod -> mod.copy(volumesOwned = it) } }
            )
        }
        LibraryEditRatingField(entry = entry, onShowRatingSheet = onShowRatingSheet)
        LibraryEditReconsumeField(entry = entry, isAnime = isAnime, viewModel = viewModel)
        LibraryEditPrivacyField(
            isPrivate = entry.isPrivate == true,
            onPrivacyChange = { viewModel.updateLibraryEntry { mod -> mod.copy(privateEntry = it) } }
        )
        LibraryEditDateField(
            label = stringResource(R.string.library_edit_started),
            dateString = entry.startedAt,
            showClear = !entry.startedAt.isNullOrEmpty(),
            onClick = onOpenStartedDatePicker,
            onClear = { viewModel.updateLibraryEntry { mod -> mod.copy(startedAt = "") } }
        )
        LibraryEditDateField(
            label = stringResource(R.string.library_edit_finished),
            dateString = entry.finishedAt,
            showClear = !entry.finishedAt.isNullOrEmpty(),
            onClick = onOpenFinishedDatePicker,
            onClear = { viewModel.updateLibraryEntry { mod -> mod.copy(finishedAt = "") } }
        )
        LibraryEditNotesField(
            notes = entry.notes ?: "",
            onNotesChange = { viewModel.updateLibraryEntry { mod -> mod.copy(notes = it.ifBlank { null }) } }
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun LibraryEditMediaHeader(
    entry: LibraryEntryWithModification,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MediaCover(
            imageUrl = entry.media?.posterImageUrl,
            modifier = Modifier.size(width = 57.dp, height = 80.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.media?.title ?: "",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = entry.media?.subtypeFormatted ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryEditStatusField(
    status: LibraryStatus?,
    isAnime: Boolean,
    onStatusChange: (LibraryStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = status?.let { stringResource(it.getStringResId(isAnime)) } ?: ""

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.library_edit_status)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            libraryStatusOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.getStringResId(isAnime))) },
                    onClick = { onStatusChange(option); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun LibraryEditProgressField(
    entry: LibraryEntryWithModification,
    viewModel: LibraryEditEntryViewModel
) {
    val mediaEpCount = entry.media?.episodeOrChapterCount
    NumberInputRow(
        label = stringResource(R.string.library_edit_progress),
        value = entry.progress ?: 0,
        max = mediaEpCount,
        onValueChange = { newProgress ->
            if (newProgress == mediaEpCount && mediaEpCount != null) {
                viewModel.updateLibraryEntry { mod ->
                    mod.copy(progress = newProgress, status = LibraryStatus.Completed)
                }
            } else {
                viewModel.updateLibraryEntry { mod -> mod.copy(progress = newProgress) }
            }
        }
    )
}

@Composable
private fun NumberInputRow(
    label: String,
    value: Int,
    max: Int?,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            val suffix = max?.let { " / $it" } ?: ""
            Text("$value$suffix", style = MaterialTheme.typography.bodyLarge)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) { Text(actionLabel) }
                Spacer(Modifier.width(4.dp))
            }
            IconButton(
                onClick = { if (value > 0) onValueChange(value - 1) },
                enabled = value > 0
            ) { Icon(Icons.Default.Remove, contentDescription = null) }
            Text(value.toString(), modifier = Modifier.padding(horizontal = 4.dp))
            IconButton(
                onClick = { if (max == null || value < max) onValueChange(value + 1) },
                enabled = max == null || value < max
            ) { Icon(Icons.Default.Add, contentDescription = null) }
        }
    }
}

@Composable
private fun LibraryEditRatingField(
    entry: LibraryEntryWithModification,
    onShowRatingSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ratingTwenty = entry.ratingTwenty
    val hasRated = ratingTwenty != null && ratingTwenty != -1
    val ratingText = if (hasRated) {
        "${ratingTwenty!!.formatRatingTwenty()} / ${20.formatRatingTwenty()}"
    } else {
        stringResource(R.string.library_not_rated)
    }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = ratingText,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.library_edit_rating)) },
            trailingIcon = {
                Icon(
                    if (hasRated) Icons.Default.Star else Icons.Default.StarOutline,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.matchParentSize().clickable(onClick = onShowRatingSheet))
    }
}

@Composable
private fun LibraryEditReconsumeField(
    entry: LibraryEntryWithModification,
    isAnime: Boolean,
    viewModel: LibraryEditEntryViewModel
) {
    NumberInputRow(
        label = stringResource(
            if (isAnime) R.string.library_edit_rewatch_count else R.string.library_edit_reread_count
        ),
        value = entry.reconsumeCount ?: 0,
        max = null,
        onValueChange = { viewModel.updateLibraryEntry { mod -> mod.copy(reconsumeCount = it) } },
        actionLabel = stringResource(
            if (isAnime) R.string.library_edit_start_rewatch else R.string.library_edit_start_reread
        ),
        onAction = {
            viewModel.updateLibraryEntry { mod ->
                mod.copy(
                    progress = 0,
                    reconsumeCount = (entry.reconsumeCount ?: 0) + 1,
                    status = LibraryStatus.Current
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryEditPrivacyField(
    isPrivate: Boolean,
    onPrivacyChange: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val publicLabel = stringResource(R.string.library_edit_privacy_public)
    val privateLabel = stringResource(R.string.library_edit_privacy_private)
    val options = listOf(publicLabel, privateLabel)

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = if (isPrivate) privateLabel else publicLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.library_edit_privacy)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onPrivacyChange(index == 1); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun LibraryEditDateField(
    label: String,
    dateString: String?,
    showClear: Boolean,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayText = if (dateString.isNullOrEmpty()) {
        stringResource(R.string.library_edit_no_date_set)
    } else {
        dateString.parseUtcDate()?.formatDate() ?: dateString
    }

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = displayText,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.matchParentSize().clickable(onClick = onClick))
        }
        if (showClear) {
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onClear) {
                Icon(Icons.Default.Delete, stringResource(R.string.action_remove))
            }
        }
    }
}

@Composable
private fun LibraryEditNotesField(
    notes: String,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        label = { Text(stringResource(R.string.library_edit_notes)) },
        minLines = 3,
        maxLines = 6,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = modifier.fillMaxWidth()
    )
}
