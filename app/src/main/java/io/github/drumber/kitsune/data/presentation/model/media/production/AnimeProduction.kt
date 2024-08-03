package io.github.drumber.kitsune.data.presentation.model.media.production

data class AnimeProduction(
    val id: String,
    val role: AnimeProductionRole?,

    val producer: Producer?
)
