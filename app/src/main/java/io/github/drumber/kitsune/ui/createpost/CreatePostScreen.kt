package io.github.drumber.kitsune.ui.createpost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneTopAppBar
import io.github.drumber.kitsune.ui.component.compose.media.MarkdownText
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    modifier: Modifier = Modifier,
    uiState: CreatePostViewModel.UiState,
    events: Flow<CreatePostViewModel.Event>,
    /** Set to true to show a snackbar for image-encoding failures; reset via [onImageEncodingErrorShown]. */
    imageEncodingError: Boolean = false,
    onImageEncodingErrorShown: () -> Unit = {},
    onContentChange: (String) -> Unit,
    onSpoilerToggle: (Boolean) -> Unit,
    onNsfwToggle: (Boolean) -> Unit,
    onTagMediaClick: () -> Unit,
    onTagUnitClick: () -> Unit,
    onClearMedia: () -> Unit,
    onClearUnit: () -> Unit,
    onAddImageClick: () -> Unit,
    onRemoveImage: (Int) -> Unit,
    onPublish: () -> Unit,
    onNavigateUp: () -> Unit,
    /** Called (instead of [onNavigateUp]) when the post was successfully published. */
    onPublished: () -> Unit = onNavigateUp
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val loginRequiredMsg = stringResource(R.string.comment_login_required)
    val publishedMsg = stringResource(R.string.post_published)
    val updatedMsg = stringResource(R.string.post_updated)
    val errorMsg = stringResource(R.string.comment_action_failed)

    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                CreatePostViewModel.Event.LoginRequired ->
                    snackbarHostState.showSnackbar(loginRequiredMsg)

                CreatePostViewModel.Event.Published -> {
                    val msg = if (uiState.isEditMode) updatedMsg else publishedMsg
                    snackbarHostState.showSnackbar(msg)
                    onPublished()
                }

                CreatePostViewModel.Event.Error ->
                    snackbarHostState.showSnackbar(errorMsg)
            }
        }
    }

    if (imageEncodingError) {
        LaunchedEffect(imageEncodingError) {
            snackbarHostState.showSnackbar(errorMsg)
            onImageEncodingErrorShown()
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CreatePostTopBar(
                isEditMode = uiState.isEditMode,
                subtitle = buildSubtitle(
                    wallTargetName = uiState.wallTargetName,
                    groupTargetName = uiState.groupTargetName
                ),
                canPublish = uiState.canPublish,
                onPublish = onPublish,
                onNavigateUp = onNavigateUp,
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isPublishing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            CreatePostContent(
                uiState = uiState,
                onContentChange = onContentChange,
                onSpoilerToggle = onSpoilerToggle,
                onNsfwToggle = onNsfwToggle,
                onTagMediaClick = onTagMediaClick,
                onTagUnitClick = onTagUnitClick,
                onClearMedia = onClearMedia,
                onClearUnit = onClearUnit,
                onAddImageClick = onAddImageClick,
                onRemoveImage = onRemoveImage,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun buildSubtitle(wallTargetName: String?, groupTargetName: String?): String? {
    return when {
        wallTargetName != null -> stringResource(R.string.create_post_wall_hint, wallTargetName)
        groupTargetName != null -> stringResource(R.string.create_post_group_hint, groupTargetName)
        else -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePostTopBar(
    isEditMode: Boolean,
    subtitle: String?,
    canPublish: Boolean,
    onPublish: () -> Unit,
    onNavigateUp: () -> Unit,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior
) {
    val title = if (isEditMode) {
        stringResource(R.string.title_edit_post)
    } else {
        stringResource(R.string.title_create_post)
    }
    KitsuneTopAppBar(
        title = {
            Column {
                Text(title)
                if (subtitle != null) {
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        navigationIcon = { KitsuneBackButton(onNavigateUp = onNavigateUp) },
        actions = {
            Button(
                onClick = onPublish,
                enabled = canPublish,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(stringResource(R.string.action_publish))
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun CreatePostContent(
    uiState: CreatePostViewModel.UiState,
    onContentChange: (String) -> Unit,
    onSpoilerToggle: (Boolean) -> Unit,
    onNsfwToggle: (Boolean) -> Unit,
    onTagMediaClick: () -> Unit,
    onTagUnitClick: () -> Unit,
    onClearMedia: () -> Unit,
    onClearUnit: () -> Unit,
    onAddImageClick: () -> Unit,
    onRemoveImage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        PostTextInput(
            content = uiState.content,
            onContentChange = onContentChange,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        SectionLabel(stringResource(R.string.label_post_options))
        Spacer(Modifier.height(4.dp))
        PostTogglesRow(
            spoiler = uiState.spoiler,
            nsfw = uiState.nsfw,
            onSpoilerToggle = onSpoilerToggle,
            onNsfwToggle = onNsfwToggle
        )
        Spacer(Modifier.height(12.dp))
        SectionLabel(stringResource(R.string.label_add_tags))
        Spacer(Modifier.height(4.dp))
        PostTagsSection(
            media = uiState.media,
            unit = uiState.unit,
            onTagMediaClick = onTagMediaClick,
            onTagUnitClick = onTagUnitClick,
            onClearMedia = onClearMedia,
            onClearUnit = onClearUnit
        )
        Spacer(Modifier.height(8.dp))
        PostImageSection(
            images = uiState.images,
            canAddMore = uiState.images.size < CreatePostViewModel.MAX_IMAGES,
            onAddImageClick = onAddImageClick,
            onRemoveImage = onRemoveImage
        )
        if (uiState.content.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            SectionLabel(stringResource(R.string.label_preview))
            Spacer(Modifier.height(4.dp))
            MarkdownText(
                content = uiState.content,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

private const val MAX_POST_LENGTH = 9000

@Composable
private fun PostTextInput(
    content: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = content,
        onValueChange = { if (it.length <= MAX_POST_LENGTH) onContentChange(it) },
        modifier = modifier.height(140.dp),
        placeholder = { Text(stringResource(R.string.hint_post_content)) },
        supportingText = {
            Text(
                text = "${content.length}/$MAX_POST_LENGTH",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        },
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
private fun PostTagsSection(
    media: CreatePostViewModel.SelectedMedia?,
    unit: CreatePostViewModel.SelectedUnit?,
    onTagMediaClick: () -> Unit,
    onTagUnitClick: () -> Unit,
    onClearMedia: () -> Unit,
    onClearUnit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssistChip(
            onClick = onTagMediaClick,
            label = { Text(stringResource(R.string.action_tag_media)) }
        )
        AssistChip(
            onClick = onTagUnitClick,
            enabled = media != null,
            label = { Text(stringResource(R.string.action_tag_unit)) }
        )
    }
    if (media != null) {
        Spacer(Modifier.height(4.dp))
        InputChip(
            selected = true,
            onClick = {},
            label = { Text(media.title) },
            trailingIcon = {
                IconButton(onClick = onClearMedia, modifier = Modifier.size(18.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
        )
    }
    if (unit != null) {
        Spacer(Modifier.height(4.dp))
        InputChip(
            selected = true,
            onClick = {},
            label = { Text(unit.title) },
            trailingIcon = {
                IconButton(onClick = onClearUnit, modifier = Modifier.size(18.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
        )
    }
}

@Composable
private fun PostImageSection(
    images: List<CreatePostViewModel.SelectedImage>,
    canAddMore: Boolean,
    onAddImageClick: () -> Unit,
    onRemoveImage: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssistChip(
            onClick = onAddImageClick,
            enabled = canAddMore,
            label = { Text(stringResource(R.string.action_add_image)) },
            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
        )
    }
    if (images.isNotEmpty()) {
        Spacer(Modifier.height(4.dp))
        LazyRow(
            contentPadding = PaddingValues(end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(images) { index, image ->
                PostImageThumbnail(
                    imageUrl = image.uri,
                    onRemove = { onRemoveImage(index) }
                )
            }
        }
    }
}

@Composable
private fun PostImageThumbnail(imageUrl: String, onRemove: () -> Unit) {
    Box {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(72.dp)
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PostTogglesRow(
    spoiler: Boolean,
    nsfw: Boolean,
    onSpoilerToggle: (Boolean) -> Unit,
    onNsfwToggle: (Boolean) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = spoiler,
            onClick = { onSpoilerToggle(!spoiler) },
            label = { Text(stringResource(R.string.label_spoiler)) }
        )
        FilterChip(
            selected = nsfw,
            onClick = { onNsfwToggle(!nsfw) },
            label = { Text(stringResource(R.string.label_nsfw)) }
        )
    }
}
