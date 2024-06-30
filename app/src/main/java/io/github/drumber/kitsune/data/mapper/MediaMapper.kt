package io.github.drumber.kitsune.data.mapper

import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.source.network.media.NetworkMedia

object MediaMapper {
    fun NetworkMedia.toMedia(): Media = when (this) {
        // TODO
//        is NetworkMedia.Anime -> toAnime()
//        is NetworkMedia.Manga -> toManga()
        else -> throw IllegalArgumentException("Unknown media type: ${this.javaClass.name}")
    }
}