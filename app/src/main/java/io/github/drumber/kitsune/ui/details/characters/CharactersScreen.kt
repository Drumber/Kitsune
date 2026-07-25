package io.github.drumber.kitsune.ui.details.characters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.character.Character
import io.github.drumber.kitsune.data.presentation.model.media.production.Casting
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneBackButton
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneCollapsingTopAppBar
import io.github.drumber.kitsune.ui.component.compose.list.PagingColumn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharactersScreen(
    title: String,
    items: LazyPagingItems<Casting>,
    languages: List<String>,
    selectedLanguage: String?,
    onNavigateUp: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    onCharacterClick: (Character) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val layoutDir = LocalLayoutDirection.current
    Scaffold(
        topBar = {
            KitsuneCollapsingTopAppBar(
                title = { Text(title) },
                navigationIcon = { KitsuneBackButton(onNavigateUp) },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            if (languages.isNotEmpty()) {
                LanguageFilterRow(
                    languages = languages,
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = onLanguageSelected
                )
            }
            PagingColumn(
                items = items,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = paddingValues.calculateLeftPadding(layoutDir),
                    end = paddingValues.calculateRightPadding(layoutDir),
                    bottom = paddingValues.calculateBottomPadding()
                ),
                key = { it.id }
            ) { item ->
                if (item != null) {
                    CastingItem(casting = item, onCharacterClick = onCharacterClick)
                }
            }
        }
    }
}

@Composable
private fun LanguageFilterRow(
    languages: List<String>,
    selectedLanguage: String?,
    onLanguageSelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(languages, key = { it }) { language ->
            FilterChip(
                selected = language == selectedLanguage,
                onClick = { onLanguageSelected(language) },
                label = { Text(language) },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun CastingItem(casting: Casting, onCharacterClick: (Character) -> Unit) {
    val character = casting.character ?: return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCharacterClick(character) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideImage(
            model = character.image?.original,
            contentDescription = character.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(56.dp)
        ) {
            it.placeholder(R.drawable.ic_insert_photo_48).error(R.drawable.ic_insert_photo_48)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = character.name.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            casting.person?.name?.let { actorName ->
                Text(
                    text = actorName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        casting.person?.let { person ->
            Spacer(Modifier.width(12.dp))
            GlideImage(
                model = person.image?.original,
                contentDescription = person.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(40.dp)
            ) {
                it.placeholder(R.drawable.ic_insert_photo_48)
                    .error(R.drawable.ic_insert_photo_48)
            }
        }
    }
}
