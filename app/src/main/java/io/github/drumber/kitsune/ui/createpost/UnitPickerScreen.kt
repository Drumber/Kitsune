package io.github.drumber.kitsune.ui.createpost

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import io.github.drumber.kitsune.data.presentation.model.media.unit.MediaUnit
import io.github.drumber.kitsune.ui.component.compose.list.PagingColumn

@Composable
fun UnitPickerScreen(
    modifier: Modifier = Modifier,
    units: LazyPagingItems<MediaUnit>,
    posterUrl: String?,
    onUnitClick: (MediaUnit) -> Unit
) {
    PagingColumn(
        items = units,
        modifier = modifier.fillMaxSize(),
        key = { it.id ?: it.hashCode().toString() }
    ) { unit ->
        UnitRow(
            unit = unit,
            posterUrl = posterUrl,
            onClick = { unit?.let(onUnitClick) }
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun UnitRow(unit: MediaUnit?, posterUrl: String?, onClick: () -> Unit) {
    val thumbnailUrl = unit?.thumbnail?.original ?: posterUrl

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideImage(
            model = thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(width = 64.dp, height = 48.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            val numberLabel = unit?.numberText(androidx.compose.ui.platform.LocalContext.current)
            if (numberLabel != null) {
                Text(
                    text = numberLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            val title = unit?.title(androidx.compose.ui.platform.LocalContext.current)
            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
