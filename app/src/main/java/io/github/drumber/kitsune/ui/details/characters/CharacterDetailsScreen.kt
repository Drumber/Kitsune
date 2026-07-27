package io.github.drumber.kitsune.ui.details.characters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.character.MediaCharacter
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.ui.component.compose.media.ExpandableText
import io.github.drumber.kitsune.ui.component.compose.media.MediaCover

@Composable
fun CharacterDetailsScreen(
    character: Character?,
    isFavorite: Boolean,
    isLoadingMediaCharacters: Boolean,
    hasMediaCharacters: Boolean,
    mediaCharacters: List<MediaCharacter>,
    onFavoriteClick: () -> Unit,
    onOpenOnMal: () -> Unit,
    onMediaCharacterClick: (Media) -> Unit,
    onImageClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        CharacterHeader(
            character = character,
            isFavorite = isFavorite,
            onFavoriteClick = onFavoriteClick,
            onImageClick = onImageClick
        )
        CharacterInfoSection(character = character)
        CharacterAppearancesSection(
            isLoading = isLoadingMediaCharacters,
            hasData = hasMediaCharacters,
            mediaCharacters = mediaCharacters,
            onMediaCharacterClick = onMediaCharacterClick
        )
        if (character?.malId != null) {
            TextButton(
                onClick = onOpenOnMal,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 16.dp)
            ) {
                Text(stringResource(R.string.action_open_on_mal))
            }
        }
    }
}

@Composable
private fun CharacterHeader(
    character: Character?,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onImageClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = character?.name.orEmpty(),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(
                    if (isFavorite) {
                        R.string.action_remove_from_favorites
                    } else {
                        R.string.action_add_to_favorites
                    }
                )
            )
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        CharacterImage(
            imageUrl = character?.image?.originalOrDown(),
            name = character?.name,
            onImageClick = onImageClick
        )
    }
}

@Composable
private fun CharacterImage(imageUrl: String?, name: String?, onImageClick: () -> Unit) {
    AsyncImage(
        model = imageUrl,
        contentDescription = name,
        contentScale = ContentScale.Fit,
        placeholder = painterResource(R.drawable.ic_insert_photo_48),
        error = painterResource(R.drawable.ic_insert_photo_48),
        modifier = Modifier
            .size(width = 106.dp, height = 150.dp)
            .clickable(onClick = onImageClick)
    )
}

@Composable
private fun CharacterInfoSection(character: Character?) {
    val description = character?.description
    if (!description.isNullOrBlank()) {
        ExpandableText(
            text = description,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        )
    }
}

@Composable
private fun CharacterAppearancesSection(
    isLoading: Boolean,
    hasData: Boolean,
    mediaCharacters: List<MediaCharacter>,
    onMediaCharacterClick: (Media) -> Unit
) {
    Text(
        text = stringResource(R.string.title_character_appearances),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
    )
    when {
        isLoading -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        !hasData -> {
            Text(
                text = stringResource(R.string.no_data_available),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        else -> AppearanceLazyRow(
            mediaCharacters = mediaCharacters,
            onMediaCharacterClick = onMediaCharacterClick
        )
    }
}

@Composable
private fun AppearanceLazyRow(
    mediaCharacters: List<MediaCharacter>,
    onMediaCharacterClick: (Media) -> Unit
) {
    LazyRow(modifier = Modifier.fillMaxWidth()) {
        items(mediaCharacters, key = { it.id }) { mc ->
            mc.media?.let { media ->
                MediaCover(
                    imageUrl = media.posterImageUrl,
                    contentDescription = media.title,
                    modifier = Modifier
                        .size(width = 106.dp, height = 150.dp)
                        .clickable { onMediaCharacterClick(media) }
                        .padding(4.dp)
                )
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}
