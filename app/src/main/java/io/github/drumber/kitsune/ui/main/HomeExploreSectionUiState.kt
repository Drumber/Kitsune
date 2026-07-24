package io.github.drumber.kitsune.ui.main

import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.util.network.ResponseData

data class HomeExploreSectionUiState(
    val title: String,
    val response: ResponseData<List<Media>>?,
    val onHeaderClick: () -> Unit
)
