package io.github.drumber.kitsune.ui.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.mapping.Mapping
import io.github.drumber.kitsune.data.presentation.model.mapping.getExternalUrl
import io.github.drumber.kitsune.data.presentation.model.mapping.getSiteName

@Composable
fun MediaMappingsScreen(
    state: MediaMappingsSate,
    onOpenUrl: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Text(
            text = stringResource(R.string.action_open_external),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 20.dp)
        )
        when (state) {
            is MediaMappingsSate.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is MediaMappingsSate.Error -> {
                Text(
                    text = stringResource(R.string.error_mapping_loading),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
            is MediaMappingsSate.Success -> {
                val mappings = state.mappings
                    .distinctBy { it.getExternalUrl() ?: it.externalSite }
                    .sortedBy { it.externalSite }
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(mappings, key = { it.id.ifBlank { it.externalSite.orEmpty() } }) {
                        MappingItem(mapping = it, onOpenUrl = onOpenUrl)
                    }
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun MappingItem(mapping: Mapping, onOpenUrl: (String) -> Unit) {
    val url = mapping.getExternalUrl()
    val siteName = mapping.getSiteName() ?: mapping.externalSite ?: "?"
    TextButton(
        onClick = { url?.let { onOpenUrl(it) } },
        enabled = url != null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = siteName,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
    }
}
