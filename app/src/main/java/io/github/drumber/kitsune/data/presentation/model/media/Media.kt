package io.github.drumber.kitsune.data.presentation.model.media

import io.github.drumber.kitsune.data.common.model.Image
import io.github.drumber.kitsune.data.common.model.Titles
import io.github.drumber.kitsune.data.common.model.en
import io.github.drumber.kitsune.data.common.model.enJp
import io.github.drumber.kitsune.data.common.model.jaJp
import io.github.drumber.kitsune.data.common.model.media.AgeRating
import io.github.drumber.kitsune.data.common.model.media.MediaType
import io.github.drumber.kitsune.data.common.model.media.RatingFrequencies
import io.github.drumber.kitsune.data.common.model.media.ReleaseStatus
import io.github.drumber.kitsune.data.presentation.model.media.category.Category
import io.github.drumber.kitsune.data.presentation.model.media.production.AnimeProductionRole
import io.github.drumber.kitsune.data.presentation.model.media.relationship.MediaRelationship
import io.github.drumber.kitsune.data.presentation.model.user.FavoriteItem

sealed class Media : FavoriteItem {
    abstract val id: String
    abstract val slug: String?

    abstract val description: String?
    abstract val titles: Titles?
    abstract val canonicalTitle: String?
    abstract val abbreviatedTitles: List<String>?

    abstract val averageRating: String?
    abstract val ratingFrequencies: RatingFrequencies?
    abstract val userCount: Int?
    abstract val favoritesCount: Int?
    abstract val popularityRank: Int?
    abstract val ratingRank: Int?

    abstract val startDate: String?
    abstract val endDate: String?
    abstract val nextRelease: String?
    abstract val tba: String?
    abstract val status: ReleaseStatus?

    abstract val ageRating: AgeRating?
    abstract val ageRatingGuide: String?
    abstract val nsfw: Boolean?

    abstract val posterImage: Image?
    abstract val coverImage: Image?

    abstract val totalLength: Int?

    abstract val categories: List<Category>?
    abstract val mediaRelationships: List<MediaRelationship>?

    //********************************************************************************************//

    abstract val mediaType: MediaType

    val titleEn get() = titles?.en

    val titleEnJp get() = titles?.enJp

    val titleJaJp get() = titles?.jaJp

    val abbreviatedTitlesFormatted get() = abbreviatedTitles?.joinToString(", ")

    val posterImageUrl get() = posterImage?.smallOrHigher()

    val coverImageUrl get() = coverImage?.originalOrDown()

    val subtypeFormatted
        get() = when (this) {
            is Anime -> subtype
            is Manga -> subtype
        }?.name.orEmpty().replaceFirstChar(Char::titlecase)

    val ageRatingText: String?
        get() {
            var ageRatingText = ageRating?.name ?: return null
            if (ageRatingGuide != null) {
                ageRatingText += " - $ageRatingGuide"
            }
            return ageRatingText
        }

    val serializationText: String? get() = (this as? Manga)?.serialization

    val chapters: String? get() = (this as? Manga)?.chapterCount?.toString()

    val volumes: String?
        get() = (this as? Manga)?.volumeCount?.let { volumes ->
            if (volumes > 0) {
                volumes.toString()
            } else {
                null
            }
        }

    val episodes: String? get() = (this as? Anime)?.episodeCount?.toString()

    val episodeOrChapterCount
        get() = (this as? Anime)?.episodeCount ?: (this as? Manga)?.chapterCount

    val trailerUrl: String?
        get() = if (this is Anime && !youtubeVideoId.isNullOrBlank()) {
            "https://www.youtube.com/watch?v=$youtubeVideoId"
        } else null

    val trailerCoverUrl: String?
        get() = if (this is Anime && !youtubeVideoId.isNullOrBlank()) {
            "https://img.youtube.com/vi/$youtubeVideoId/mqdefault.jpg"
        } else null

    fun getProducer(role: AnimeProductionRole): String? {
        return (this as? Anime)?.animeProduction?.filter { it.role == role }
            ?.mapNotNull { it.producer?.name }
            ?.distinct()
            ?.joinToString(", ")
    }

    fun hasStreamingLinks() = this is Anime && !streamingLinks.isNullOrEmpty()

    fun hasMediaRelationships() = !mediaRelationships.isNullOrEmpty()

    fun hasRatingFrequencies() = ratingFrequencies != null
}